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
 * FIXME may fail (StackOverFlow) when there is a cycle (see {@link AbstractTraverser}; use {@link org.biopax.paxtool
  s.util.Filter})
 * 
 * @see org.biopax.paxtools.controller.Visitor
 * @see org.biopax.paxtools.controller.Traverser
 *
 */
public class Fetcher extends AbstractTraverser
{
    private final Set<BioPAXElement> children;
	
	public Fetcher(EditorMap editorMap, Filter<PropertyEditor>... filters) {
        super(editorMap, filters);
        this.children = new HashSet<BioPAXElement>();
    }

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
    	children.clear();
    	super.traverse(element, null);
        model.add(element);
        for(BioPAXElement e : children)
        	if(!model.contains(e))
        		model.add(e);
	}
    
    /**
     * Returns the element and all its children set.
     * (This method can return different objects
     * with the same ID!)
     * 
     * @param element
     * @param model
     */
    public Set<BioPAXElement> fetch(BioPAXElement element)
	{
    	children.clear();
    	super.traverse(element, null);
        return new HashSet<BioPAXElement>(children);
	}
}
