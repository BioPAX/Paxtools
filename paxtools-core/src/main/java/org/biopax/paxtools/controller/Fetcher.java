package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;

import javax.swing.*;

/**
 * This class is used to fetch an element (traverse it to obtain
 * its dependent elements) and to add this element into a model
 * using the visitor and traverse functions.
 *
 * FIXME may fail (StackOverFlow) when there is a cycle (see {@link AbstractIterativeTraverser}; use {@link PropertyFilter})
 * 
 * @see org.biopax.paxtools.controller.Visitor
 * @see org.biopax.paxtools.controller.Traverser
 *
 */
public class Fetcher extends AbstractIterativeTraverser
{



    public Fetcher(EditorMap editorMap, PropertyFilter... filters) {
        super(editorMap, filters);
    }

    /**
     * Adds the BioPAX element into the model and traverses the element
     * for its dependent elements.
     *
     * @param domain
     * @param range
     * @param model model into which the element will be added
     * @param editor editor that is going to be used for traversing functionality   
     * @see org.biopax.paxtools.controller.Traverser
     * 
     */
     @Override
    protected void visit(Object range, BioPAXElement domain, Model model, PropertyEditor editor)
    {
		if (range != null && range instanceof BioPAXElement && !model.getObjects().contains(range))
        {
			model.add((BioPAXElement) range);
		}
	}

    /**
     * Traverses and adds the element into the model.
     *
     * @param element the BioPAX element to be added into the model
     * @param model model into which the element will be added
     *
     * @see org.biopax.paxtools.controller.Traverser
     *
     */
    public void fetch(BioPAXElement element, Model model)
	{
    	super.traverse(element, model);
        model.add(element);
	}
}
