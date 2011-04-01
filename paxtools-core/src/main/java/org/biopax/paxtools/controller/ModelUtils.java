package org.biopax.paxtools.controller;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.impl.BioPAXFactoryAdaptor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.Level2Element;
import org.biopax.paxtools.util.ClassFilterSet;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

/**
 * An advanced BioPAX utility class to
 * find root elements, remove dangling,
 * replace elements or/and identifiers
 * in the model, etc.
 * 
 * @author rodche
 *
 */
public class ModelUtils {
	private static final Log LOG = LogFactory.getLog(ModelUtils.class);
	
	private final Model model; // a model to hack ;)
	private final EditorMap editorMap;
	
	/**
	 * Constructor.
	 * 
	 * @param model
	 */
	public ModelUtils(Model model) 
	{
		this.model = model;
		this.editorMap = new SimpleEditorMap(model.getLevel());
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
				if(range instanceof BioPAXElement && range.equals(existing))
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
			BioPAXLevel level = (e instanceof Level2Element) ? BioPAXLevel.L2 : BioPAXLevel.L3;
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
    
    
	/**
	 * Finds a subset of "root" BioPAX objects of the specified class 
	 * (and sub-classes) in the model.
	 * 
	 * @param filterClass 
	 * @return
	 */
	public <T extends BioPAXElement> Set<T> getRootElements(final Class<T> filterClass) {
		return getRootElements(model.getObjects(), filterClass);
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
	 * Finds a subset of "root" BioPAX objects of specific class (incl. sub-classes;
	 * also works with a mix of different level biopax elements.)
	 * 
	 * Note: however, such "root" elements may or may not be, a property of other
	 * elements, not included in the query (source) set.
	 * 
	 * @author rodche
	 * @param sourceSet - model elements that can be "children" (property value) of each-other
	 * @param filterClass 
	 * @return
	 */
	public static <T extends BioPAXElement> Set<T> getRootElements(Set<BioPAXElement> sourceSet, 
			final Class<T> filterClass) 
	{
		// copy all such elements (initially, we think all are roots...)
		final Set<T> result = new HashSet<T>();
		result.addAll(new ClassFilterSet<T>(sourceSet, filterClass));
		
		for(BioPAXElement e : sourceSet) {
			// define a special 'visitor'
			BioPAXLevel level = (e instanceof Level2Element) ? BioPAXLevel.L2 : BioPAXLevel.L3;
			AbstractTraverser traverser = new AbstractTraverser(new SimpleEditorMap(level)) 
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
	
}
