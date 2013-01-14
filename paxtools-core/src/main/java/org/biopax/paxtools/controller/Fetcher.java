package org.biopax.paxtools.controller;

import java.util.HashSet;
import java.util.Set;

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
     * (This method can return different objects
     * with the same ID!)
     * 
     * @param element
     * @param model
     */
    public Set<BioPAXElement> fetch(BioPAXElement element)
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
    				super.traverse(bpe, model);
    			}
    		}
    	};

    	traverser.traverse(element, null);
    	
    	return children;
	}
}
