package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;

/**
 * This class is used to fetch an element (traverse it to obtain
 * its dependent elements) and to add this element into a model
 * using the visitor and traverse functionalities.
 *
 * @see org.biopax.paxtools.controller.Visitor
 * @see org.biopax.paxtools.controller.Traverser
 *
 */
public class Fetcher implements Visitor
{
// ------------------------------ FIELDS ------------------------------

	Traverser traverser;

// --------------------------- CONSTRUCTORS ---------------------------

	public Fetcher(EditorMap map)
	{
		traverser = new Traverser(map, this);
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Visitor ---------------------

    /**
     * Adds the BioPAX element into the model and traverses the element
     * for its dependent elements.
     *
     * @param bpe the BioPAX element to be added into the model
     * @param model model into which the element will be added
     * @param editor editor that is going to be used for traversing functionallity
     *
     * @see org.biopax.paxtools.controller.Traverser
     * 
     */
    public void visit(BioPAXElement bpe, Model model, PropertyEditor editor)
	{
		if (bpe != null && !model.getObjects().contains(bpe))
		{
			model.add(bpe);
			traverser.traverse(bpe, model);
		}
	}

// -------------------------- OTHER METHODS --------------------------

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
		traverser.traverse(element, model);
	}
}
