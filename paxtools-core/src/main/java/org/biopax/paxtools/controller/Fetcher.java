package org.biopax.paxtools.controller;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.ArrayUtils;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.util.Filter;

/**
 * This class is used to fetch an element (traverse it to obtain
 * its dependent elements) and to add this element into a model
 * using the visitor and traverse functions.
 *
 * Must be thread safe if the property filters and {@link EditorMap} 
 * passed to the constructor are safe.
 * 
 * @see org.biopax.paxtools.controller.Visitor
 * @see org.biopax.paxtools.controller.Traverser
 *
 */
public class Fetcher {
    
	private final EditorMap editorMap;
    private final Filter<PropertyEditor>[] filters;

	private boolean skipSubPathways;

	/**
	 * This property filter can be used to ignore 'nextStep' ('NEXT-STEP' in L2) 
	 * property when fetching a sub-graph of child biopax elements, because
	 * using this property can eventually lead outside current pathway context
	 * into peer pathways, etc. (in practice, for a pathway, all its child elements 
	 * can be reached via 'pathwayComponent', 'pathwayOrder/stepProcess', 
	 * 'pathwayOrder/stepConvertion' etc. properties; so ignoring the 'nextStep' usually OK).
	 * So, this is recommended filter for common BioPAX fetching/traversal tasks, such as
	 * to find all proteins or xrefs within a pathway and its sub-pathways, etc.
	 */
	@SuppressWarnings("rawtypes")
	public static final Filter<PropertyEditor> nextStepFilter = new Filter<PropertyEditor>()
	{
		public boolean filter(PropertyEditor editor)
		{
			return !editor.getProperty().equals("nextStep") && !editor.getProperty().equals("NEXT-STEP");
		}
	};
	

	/**
	 * A property filter to ignore 'evidence' ('EVIDENCE' in L2) property
	 * (it can eventually lead to other organism, experimental entities)
	 */
	@SuppressWarnings("rawtypes")
	public static final Filter<PropertyEditor> evidenceFilter = new Filter<PropertyEditor>()
	{
		public boolean filter(PropertyEditor editor)
		{
			return !editor.getProperty().equals("evidence") && !editor.getProperty().equals("EVIDENCE");
		}
	};


	private final Filter<PropertyEditor> objectPropertiesOnlyFilter = new Filter<PropertyEditor>()
	{
		public boolean filter(PropertyEditor editor)
		{
			return (editor instanceof ObjectPropertyEditor);
		}
	};

	
    /**
     * Constructor.
     * 
     * @param editorMap BioPAX property editors map implementation
     * @param filters optional, biopax object property filters 
     *        to skip traversing/visiting into some object property values
	 *        (the default 'object properties only' filter to is always enabled).
     */
	public Fetcher(EditorMap editorMap, Filter<PropertyEditor>... filters) {
        this.editorMap = editorMap;
        this.filters = (Filter<PropertyEditor>[])ArrayUtils.add(filters, objectPropertiesOnlyFilter);
		this.skipSubPathways = false;
    }


	/**
	 * Use this property to optionally
	 * skip (if true) traversing into sub-pathways;
	 * i.e., when a biopax property, such as pathwayComponent
	 * or controlled, value is a Pathway.
	 *
	 * @param skipSubPathways
	 */
	public void setSkipSubPathways(boolean skipSubPathways) {
		this.skipSubPathways = skipSubPathways;
	}

	public boolean isSkipSubPathways() {
		return skipSubPathways;
	}

	/**
     * Adds the element and all its children
	 * (found via traversing into object properties that
	 * pass all the filters defined in the Constructor, and
	 * also taking #isSkipSubPathways into account)
	 * to the target model.
     *
     * This method fails if there are different child objects
     * with the same ID, because normally a good (self-consistent) 
     * model does not contain duplicate BioPAX elements. Consider
     * using {@link #fetch(BioPAXElement)} method instead if you 
     * want to get all the child elements anyway.
     *
     * @param element the BioPAX element to be added into the model
     * @param model model into which elements will be added
     */
    public void fetch(final BioPAXElement element, final Model model)
	{
    	if(!model.containsID(element.getRDFId()))
    		model.add(element);
    	
    	Set<BioPAXElement> children = fetch(element);
    	
        for(BioPAXElement e : children) {
			if (!model.containsID(e.getRDFId())) {
				model.add(e);
			} else if (!model.contains(e)) {
				throw new AssertionError("fetch(bioPAXElement, model): found different child objects " +
						"with the same URI: " + e.getRDFId() +
						"(replace/merge, or use fetch(bioPAXElement) instead!)");
			}
		}
	}
 
    
    /**
     * Recursively finds and collects all child objects,
	 * while escaping possible infinite loops.
     * 
     * This method can eventually return
     * different objects with the same URI if these
     * are present among child elements.
     * 
     * @param element to traverse into
     * @return a set of child biopax objects
     */
    public Set<BioPAXElement> fetch(final BioPAXElement element) {
    	return fetch(element, BioPAXElement.class);
    }
    
    
    /**
     * Recursively collects unique child objects from
	 * BioPAX object type properties that pass
	 * all the filters (as set via Constructor).
	 *
	 * The #isSkipSubPathways flag is ignored.
     * 
     * Note: this method might return
     * different objects with the same URI if such
     * are present among the child elements for some reason
	 * (in a self-integral BioPAX Model, this should never be allowed,
	 * but can happen as the result of cloning/replacing in some other methods).
     * 
     * @param bpe biopax object to traverse into properties of
     * @param depth >0; 1 means - get only direct children, 2 - include children of children, etc.;
     * @return set of child objects
	 * @throws IllegalArgumentException when depth <= 0
     */
    public Set<BioPAXElement> fetch(final BioPAXElement bpe, int depth)
	{
		//a sanity check
		if(depth <= 0) {
			throw new IllegalArgumentException("fetch(..), not a positive 'depth':" + depth);
		}

		final Set<BioPAXElement> children = new HashSet<BioPAXElement>();

		//create a simple traverser to collect direct child elements
		Traverser traverser = new Traverser(SimpleEditorMap.L3,
				new Visitor() {
					@Override
					public void visit(BioPAXElement domain, Object range, Model model, PropertyEditor<?, ?> editor) {
							children.add((BioPAXElement) range);
					}
				}, filters);
		//run
		traverser.traverse(bpe, null);

		if(!children.isEmpty() && --depth > 0) {
			for (BioPAXElement element : new HashSet<BioPAXElement>(children)) {
				Set<BioPAXElement> nextLevelElements = fetch(element, depth); //recursion goes on
				children.addAll(nextLevelElements);
			}
		}

		//remove itself (if added due to tricky loops in the model...)
		children.remove(bpe);

		return children;
	}

    
    /**
     * Goes over object type biopax properties to collect nested objects
	 * (using only properties that pass all the filters set in Constructor,
	 * and taking #isSkipSubPathways into account)
	 * of the given biopax element, its children, etc.
	 * (it also escapes any infinite semantic loops in the biopax model)
	 * It saves only biopax objects of the given type, incl. sub-types.
     * 
     * Note: this method can eventually return
     * different objects with the same URI if these
     * are present among child elements.
     * 
     * @param element to fetch child objects from
     * @param filterByType biopax type filter
     * @param <T> biopax type
     * @return set of biopax objects
     */
    public <T extends BioPAXElement> Set<T> fetch(final BioPAXElement element, final Class<T> filterByType)
	{
    	final Set<T> children = new HashSet<T>();

		AbstractTraverser traverser = new AbstractTraverser(editorMap, filters) {
    	    /*
    	     * Adds the BioPAX element into the model and traverses the element's properties
    	     * to collect all dependent/nested elements.
    	     */
    	    protected void visit(Object range, BioPAXElement domain, Model model, PropertyEditor editor)
    		{
   				//by design (see Constructor, filters), it'll visit only object properties.
				BioPAXElement bpe = (BioPAXElement) range;

				if(filterByType.isInstance(bpe)) {
					children.add((T) bpe);
				}

				if(!(skipSubPathways && (range instanceof Pathway)))
					traverse(bpe, null); //go deeper only if it's a new object
			}
    	};

    	traverser.traverse(element, null);
    	
    	return children;
	}
    
    
    /**
     * Iterates over child objects of the given biopax element, 
     * using BioPAX object-type properties, until the element 
     * with specified URI and class (including its sub-classes). 
     * is found.
     * 
     * @param root biopax element to process
     * @param uri URI to match
     * @param type class to match
     * 
     * @return true if the match found; false - otherwise
     */
    public boolean subgraphContains(final BioPAXElement root, final String uri, 
    		final Class<? extends BioPAXElement> type) {
    	
    	final AtomicBoolean found = new AtomicBoolean(false);
    	
    	Traverser traverser = new AbstractTraverser(editorMap, filters) {  
    		
    	    @Override
    	    protected void visit(Object range, BioPAXElement domain, Model model, PropertyEditor editor)
    		{
    			if (range instanceof BioPAXElement && !found.get())
    			{
    				if( ((BioPAXElement) range).getRDFId().equals(uri) )
    					found.set(true); //set global flag; done.
    				else
						if(!(skipSubPathways && (range instanceof Pathway)))
    						traverse((BioPAXElement)range, model);
    			}
    		}
    	};

    	traverser.traverse(root, null);
    	
    	return found.get();
	}
}
