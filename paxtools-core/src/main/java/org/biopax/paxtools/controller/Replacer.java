package org.biopax.paxtools.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.impl.BioPAXFactoryAdaptor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

/**
 * An advanced BioPAX utility class to
 * replace elements or/and identifiers
 * in the model.
 * 
 * @author rodche
 *
 */
public class Replacer {
	private static final Log LOG = LogFactory.getLog(Replacer.class);
	
	private final Model model; // a model to hack ;)
	
	/**
	 * Constructor.
	 * 
	 * @param model
	 */
	public Replacer(Model model) 
	{
		this.model = model;
	}
	
    /**
     * Replaces existing BioPAX element with another one,
     * of the same or possibly equivalent type or null,
     * and updates all the affected references to it 
     * (parents' object properties).
     * 
     * @param existing
     * @param replacement
     * @param isRefresh
     */
	// when true, - also delete children (which may "dirty" the model, i.e., become dangling or useless)
	// auto-add new children to the model and fix properties (via self-merging)
    public void replace(final BioPAXElement existing, final BioPAXElement replacement, boolean isRefresh) 
    {
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
		
		// optionally, fix properties/add children
		if (isRefresh) {
			// remove 'existing' and all its children from the model
			Fetcher fetcher = new Fetcher(em);
			Model old = model.getLevel().getDefaultFactory().createModel();
			fetcher.fetch(existing, old);
			for (BioPAXElement o : old.getObjects()) {
				model.remove(o);
			}
			// restore - new children to the model
			model.merge(model);
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
     * The method {@link #replace(BioPAXElement, BioPAXElement, boolean)} 
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
				/* too much unsafe...
				// find/update the object with oldId which is stored under another map key
				Set<BioPAXElement> objs = new HashSet<BioPAXElement>(model.getObjects());
				for(BioPAXElement o : objs) {
					if(oldId.equals(o.getRDFId())) {
						hackFactory.setId(old, newId);
					}
				}
				*/
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
}
