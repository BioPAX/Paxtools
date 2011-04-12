package org.biopax.paxtools.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.impl.BioPAXFactoryAdaptor;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.*;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

/**
 * An advanced BioPAX utility class that implements
 * several useful algorithms to extract root or child 
 * BioPAX elements, remove dangling, replace elements 
 * or identifiers, etc.
 * 
 * @author rodche
 *
 */
public class ModelUtils {
	private static final Log LOG = LogFactory.getLog(ModelUtils.class);
	
	/* 
	 * to ignore 'nextStep' property in most algorithms, 
	 * because it can eventually lead outside the current pathway, 
	 * and normally it (and pathwayOrder) is not necessary 
	 * for (step) processes to be reached (because they must be 
	 * listed in the pathwayComponent property as well).
	 */
	private static final PropertyFilter nextStepFilter = new PropertyFilter() {
		@Override
		public boolean filter(PropertyEditor editor) {
			return !editor.getProperty().equals("nextStep")
				&& !editor.getProperty().equals("NEXT-STEP");
		}
	};
	
	private final Model model; // a model to hack ;)
	private final EditorMap editorMap;
	private final BioPAXIOHandler io;
	
	/**
	 * Constructor.
	 * 
	 * @param model
	 */
	public ModelUtils(Model model) 
	{
		this.model = model;
		this.editorMap = new SimpleEditorMap(model.getLevel());
		this.io = new SimpleIOHandler(model.getLevel());
		((SimpleIOHandler) this.io).mergeDuplicates(true);
	}
	
    /**
     * Replaces existing BioPAX element with another one,
     * of the same or possibly equivalent type (or null),
     * recursively updates all the references to it 
     * (parents' object properties).
     * 
     * @param existing
     * @param replacement
     */
    public void replace(final BioPAXElement existing, final BioPAXElement replacement) 
    {
    	if(replacement != null && 
    		existing.getModelInterface() != replacement.getModelInterface()) {
    		LOG.error("Cannot replace " + existing.getRDFId()
    				+ " (" + existing.getModelInterface().getSimpleName() 
    				+ ") with a different type object: "
    				+ replacement.getRDFId() + " (" 
    				+ replacement.getModelInterface().getSimpleName() + ")!");
    		return;
    	}
    	
    	// first, check if "shortcut" is possible
    	// nothing to replace?
    	if(!model.contains(existing)) {
    		if(LOG.isWarnEnabled())
    			LOG.warn("Model does not contain element " + existing);
    		return;
    	}
    	
    	/* ( here goes a tough concern ;) )
    	 * fail if the 'replacement' for 'existing' object
    	 * (with different ID) uses the same ID as another,
    	 * also different, model object! That means, 
    	 */
    	if(replacement != null) { 
    		String newId = replacement.getRDFId();
    		if(model.containsID(newId) // there is an object with the same ID,
    			&& !newId.equals(existing.getRDFId()) // and it is not the one to be replaced,
    			&& replacement != model.getByID(newId)) // and the replacement is not that, then - fail
    		{
    			throw new IllegalBioPAXArgumentException(
    				"There is another object with the same ID as replacemet's," +
    				" and it is not the same nor the one to be replaced! " +
    				"Try (decide) either to replace/remove that one first, " +
    				"or - get and use that one as the replacement instead.");
    		}
    	}
    	
    	// a visitor to replace the element in the model
		Visitor visitor = new Visitor() {
			@Override
			public void visit(BioPAXElement domain, Object range, Model model, PropertyEditor editor) {
				if(range instanceof BioPAXElement && range.equals(existing)) //it's Object.equals (not just RDFId)!
				{
					if(editor.isMultipleCardinality()) {
						if(replacement != null)
							editor.setValueToBean(replacement, domain);
						editor.removeValueFromBean(existing, domain);
					} else {
						editor.setValueToBean(replacement, domain);
					}
				}
			}
		};
		
		// run to update parent's properties with the new value ('replacement')
		EditorMap em = new SimpleEditorMap(model.getLevel());
		Traverser traverser = new Traverser(em, visitor);
		for(BioPAXElement bpe : model.getObjects()) {
			traverser.traverse(bpe, null);
		}
		
		// remove the old one from the model
		model.remove(existing);

		// add the replacement
		if(replacement != null && !model.contains(replacement)) {
			model.add(replacement);
		}
    }

    
    /**
     * Deletes (recursively from the current model) 
     * only those child elements that would become "dangling" 
     * (not a property value of anything) if the parent 
     * element were (or already was) removed from the model.
     * 
     * @param parent
     */
    public void removeDependentsIfDangling(BioPAXElement parent) 
    {	
		// get the parent and all its children
		Fetcher fetcher = new Fetcher(editorMap);
		Model childModel = model.getLevel().getDefaultFactory().createModel();
		fetcher.fetch(parent, childModel);
		
		// copy all elements
		Set<BioPAXElement> others = new HashSet<BioPAXElement>(model.getObjects());
		
		// retain only those not the parent nor its child
		// (asymmetric difference)
		others.removeAll(childModel.getObjects());
		
		// traverse from each of "others" to exclude those from "children" that are used
		for(BioPAXElement e : others) {
			final BioPAXElement bpe = e;
			// define a special 'visitor'
			AbstractTraverser traverser = new AbstractTraverser(editorMap) 
			{
				@Override
				protected void visit(Object value, BioPAXElement parent, 
						Model m, PropertyEditor editor) 
				{
					if(value instanceof BioPAXElement 
							&& m.contains((BioPAXElement) value)) {
						m.remove((BioPAXElement) value); 
					}
				}
			};
			// check all biopax properties
			traverser.traverse(e, childModel);
		}
			
		// remove those left (would be dangling if parent were removed)!
		for (BioPAXElement o : childModel.getObjects()) {
			model.remove(o);
		}
    }
    
    
    /**
     * Replaces the RDFId of the BioPAX element
     * and in the current model. 
     * 
     * WARN: this hacker's method is to use with great care, 
     * because, if more than one BioPAX models share 
     * the (oldId) element, all these, except for current one,
     * will be broken (contain the updated element under its oldId) 
     * One can call {@link Model#repair()} to fix.
     * 
     * The method {@link #replace(BioPAXElement, BioPAXElement)} 
     * is much safer but less efficient in special cases, such as when 
     * one just needs to create/import one model, quickly update several or all
     * IDs, and save it to a file. 
     * 
     * @param oldId
     * @param newId
     */
    public void replaceID(String oldId, String newId) {
    	// fail if object with the newId or anything stored under this map key exists
		if(model.containsID(newId)) 
			throw new IllegalBioPAXArgumentException("Model already has ID: " + newId);

		InternalBioPAXFactory hackFactory = new InternalBioPAXFactory();
		
		BioPAXElement old = model.getByID(oldId);
		if(old != null) {
			// update if getById returns and object with the same ID
			if(oldId.equals(old.getRDFId())) {
				model.remove(old);
				hackFactory.setId(old, newId);
				model.add(old);
			} else {
				// for the broken model - skip
				if(LOG.isWarnEnabled())
					LOG.warn("Cannot replace ID. Element known by ID: " 
						+ oldId + ", in fact, has another ID: " 
						+ old.getRDFId());
			}
		} else {
			if(LOG.isWarnEnabled())
				LOG.warn("Cannot replace ID. Element is not found by ID: " + oldId);
		}
    }
    
    
    /* 
     * Extend the factory class to open up the setId method
     */
	private class InternalBioPAXFactory extends BioPAXFactoryAdaptor {
		@Override
		public BioPAXLevel getLevel() {
			return model.getLevel();
		}
		
		@Override
		protected <T extends BioPAXElement> T createInstance(Class<T> aClass,
				String id) throws ClassNotFoundException, InstantiationException,
				IllegalAccessException {
			throw new UnsupportedOperationException();
		}
		
		// this one we are going to need
		@Override
		public void setId(BioPAXElement bpe, String uri) {
			super.setId(bpe, uri);
		}
	}
	
	
	public Model getModel() {
		return model;
	}
	
	
	/**
	 * Finds a subset of "root" BioPAX objects of specific class (incl. sub-classes)
	 * 
	 * Note: however, such "root" elements may or may not be, a property of other
	 * elements, not included in the model.
	 * 
	 * @param filterClass 
	 * @return
	 */
	public <T extends BioPAXElement> Set<T> getRootElements(final Class<T> filterClass) 
	{
		// copy all such elements (initially, we think all are roots...)
		final Set<T> result = new HashSet<T>();
		result.addAll(model.getObjects(filterClass));
		
		// but we run from every element (all types)
		for(BioPAXElement e : model.getObjects()) {
			// define a special 'visitor'
			AbstractTraverser traverser = new AbstractTraverser(editorMap) 
			{
				@Override
				protected void visit(Object value, BioPAXElement parent, 
						Model model, PropertyEditor editor) {
					if(filterClass.isInstance(value)) 
						result.remove(value); 
				}
			};
			// check all biopax properties
			traverser.traverse(e, null);
		}
		
		return result;
	}
	
	/**
	 * Iteratively removes dangling elements
	 * of given type, e.g., utility class,  
	 * from current model.
	 */
	public <T extends BioPAXElement> void removeObjectsIfDangling(Class<T> clazz) 
	{
		Set<T> dangling = getRootElements(clazz);
		// get rid of dangling objects
		if(!dangling.isEmpty()) {
			if(LOG.isInfoEnabled()) 
				LOG.info(dangling.size() + " BioPAX utility objects " +
						"were/became dangling, and they "
						+ " will be deleted...");
			if(LOG.isDebugEnabled())
				LOG.debug("to remove (dangling after merge) :" + dangling);

			for(BioPAXElement thing : dangling) {
				model.remove(thing);
			}
			
			// some may have become dangling now, so check again...
			removeObjectsIfDangling(clazz);
		}
	}
	

	/**
	 * For the current (internal) model, this method 
	 * iteratively copies given property values 
	 * from parent BioPAX elements to children.
	 * If the property is multiple cardinality property, it will add
	 * new values, otherwise - it will set it only if was empty; 
	 * in both cases it won't delete/override existing values!
	 * 
	 * @see PropertyReasoner
	 * 
	 * @param property property name
	 * @param forClasses (optional) infer/set the property for these types only
	 */
	public void inferPropertyFromParent(final String property, 
			final Class<? extends BioPAXElement>... forClasses) 
	{		
		// for each ROOT element (puts a strict top-down order on the following)
		Set<BioPAXElement> roots = getRootElements(BioPAXElement.class);
		for(BioPAXElement bpe : roots) {
			PropertyReasoner reasoner = new PropertyReasoner(property, editorMap);
			reasoner.setDomains(forClasses);
			reasoner.inferPropertyValue(bpe);
		}
	}
		
	/**
	 * Cuts the BioPAX model off other models and BioPAX objects 
	 * by essentially performing write/read to/from OWL. 
	 * The resulting model contains new objects with same IDs 
	 * and have object properties "fixed", i.e., dangling values 
	 * become null/empty, and inverse properties (e.g. xrefOf)
	 * re-calculated. The original model is unchanged.
	 * 
	 * @return copy of the model
	 * @throws IOException 
	 */
	public Model writeRead()
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		io.convertToOWL(model, baos);
		return io.convertFromOWL(new ByteArrayInputStream(baos.toByteArray()));
	}	

	
	/**
	 * Gets all the child BioPAX elements of a given BioPAX element
	 * (using the "tuned" {@link Fetcher}) and adds them to a 
	 * new model.
	 * 
	 * @param bpe
	 * @return
	 */
	public Model getAllChildren(BioPAXElement bpe) {
		Model model = this.model.getLevel().getDefaultFactory().createModel();
		new Fetcher(editorMap, nextStepFilter).fetch(bpe, model);
		model.remove(bpe); // remove the parent
		
		return model;
		//TODO limit to elements from this.model (add extra parameter)?
	}
	
	
	/**
	 * Gets direct children of a given BioPAX element
	 * and adds them to a new model.
	 * 
	 * @param bpe
	 * @return
	 */
	public Model getDirectChildren(BioPAXElement bpe) 
	{	
		Model model = this.model.getLevel().getDefaultFactory().createModel();
		
		AbstractTraverser traverser = new AbstractTraverser(editorMap, nextStepFilter) {
			@Override
			protected void visit(Object range, BioPAXElement domain,
					Model model, PropertyEditor editor) {
				if (range instanceof BioPAXElement 
						&& !model.contains((BioPAXElement) range)) {
					model.add((BioPAXElement) range);
				}
			}
		};
		
		traverser.traverse(bpe, model);
		
		return model;
		//TODO limit to elements from this.model (add extra parameter)?
	}
	
}
