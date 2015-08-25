package org.biopax.paxtools.controller;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
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

	
    /**
     * Constructor.
     * 
     * @param editorMap BioPAX property editors map implementation
     * @param filters optional, biopax object property filters 
     *        to skip traversing/visiting into some property values.
     */
	public Fetcher(EditorMap editorMap, Filter<PropertyEditor>... filters) {
        this.editorMap = editorMap;
        this.filters = filters;
    }


    /**
     * Adds the element and all its children to the model.
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
    public void fetch(BioPAXElement element, Model model)
	{
    	if(!model.containsID(element.getRDFId()))
    		model.add(element);
    	
    	Set<BioPAXElement> children = fetch(element);
    	
        for(BioPAXElement e : children)
        	if(!model.containsID(e.getRDFId())) {
        		model.add(e);
        	} else if(!model.contains(e)) 
        		throw new AssertionError(
        		"fetch(bioPAXElement, model): found different child objects " +
        		"with the same URI: " + e.getRDFId() +
        		"(replace/merge, or use fetch(bioPAXElement) instead!)"); 
	}
 
    
    /**
     * Recursively finds and collects all child objects.
     * 
     * (warn: this method can eventually return 
     * different objects with the same URI if these
     * are present among child elements)
     * 
     * @param element to traverse into
     * @return a set of child biopax objects
     */
    public Set<BioPAXElement> fetch(BioPAXElement element) {
    	return fetch(element, -1);
    }
    
    
    /**
     * Recursively finds and collects all child objects.
     * 
     * (warn: this method can eventually return 
     * different objects with the same URI if these
     * are present among child elements)
     * 
     * @param element to traverse into its properties
     * @param depth negative value means unlimited (default); 1 means get direct children only, etc.
     * @return set of child objects
     */
    public Set<BioPAXElement> fetch(BioPAXElement element, final int depth)
	{
    	final Set<BioPAXElement> children = new HashSet<BioPAXElement>();
    	
    	Traverser traverser = new AbstractTraverser(editorMap, filters) {  		
    	    /**
    	     * Adds the BioPAX element into the model and traverses the element
    	     * for its dependent elements.
    	     */
    	    @Override
    	    protected void visit(Object range, BioPAXElement domain, Model model, PropertyEditor editor)
    		{
    			if (range instanceof BioPAXElement && !children.contains((BioPAXElement) range))
    			{
    				BioPAXElement bpe = (BioPAXElement) range;
    				children.add(bpe);
    				if(depth < 1 || depth > getVisited().size()) //TODO make sure visited.size is current depth...
    					traverse(bpe, model);
    			}
    		}
    	};

    	traverser.traverse(element, null);
    	
    	return children;
	}
    
    
    
    /**
     * Recursively goes over child objects of the given biopax element, 
     * its child's child objects, etc., but collects only biopax 
     * elements of the specified type (including its sub-classes).
     * 
     * (warn: this method can eventually return 
     * different objects with the same URI if these
     * are present among child elements)
     * 
     * @param element to fetch child objects from
     * @param filterByType biopax type filter
     * @param <T> biopax type
     * @return set of biopax objects
     */
    public <T extends BioPAXElement> Set<T> fetch(final BioPAXElement element, final Class<T> filterByType)
	{
    	final Set<T> children = new HashSet<T>();
    	
    	Traverser traverser = new AbstractTraverser(editorMap, filters) {  		
    	    /**
    	     * Adds the BioPAX element into the model and traverses the element
    	     * for its dependent elements.
    	     */
    	    @Override
    	    protected void visit(Object range, BioPAXElement domain, Model model, PropertyEditor editor)
    		{
    			if (range instanceof BioPAXElement && !children.contains((BioPAXElement) range))
    			{
    				BioPAXElement bpe = (BioPAXElement) range;
    				if(filterByType.isInstance(bpe))
    					children.add((T) bpe);
    				//continue deeper...
    				traverse(bpe, model);
    			}
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
    					traverse((BioPAXElement)range, model);
    			}
    		}
    	};

    	traverser.traverse(root, null);
    	
    	return found.get();
	}
}
