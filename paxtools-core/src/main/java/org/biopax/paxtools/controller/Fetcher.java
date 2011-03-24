package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;

/**
 * This class is used to fetch an element (traverse it to obtain
 * its dependent elements) and to add this element into a model
 * using the visitor and traverse functionalities.
 *
 * FIXME may fail (StackOverFlow) when there is a cycle (see {@link AbstractTraverser}; use {@link PropertyFilter})
 * 
 * @see org.biopax.paxtools.controller.Visitor
 * @see org.biopax.paxtools.controller.Traverser
 *
 */
public class Fetcher implements Visitor
{
// ------------------------------ FIELDS ------------------------------

	protected Traverser traverser;

// --------------------------- CONSTRUCTORS ---------------------------

	
	/**
	 * Constructor
	 * 
	 * Built-in Traverser is going to use this class as the Visitor
	 * implementation (designed to recursively collect child BioPAX elements)
	 *
	 * @param map EditorMap
	 */
	public Fetcher(EditorMap map)
	{
		traverser = new Traverser(map, this);
	}

	/**
	* Constructor that uses property filters
	*
	*/
	public Fetcher(EditorMap map, PropertyFilter... filters)
	{
		traverser = new Traverser(map, this, filters);
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Visitor ---------------------

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
    public void visit(BioPAXElement domain, Object range, Model model, PropertyEditor editor)
	{
    	//TODO ?? use a PropertyFilter (in Traverser's constructor) that checks "editor instanceof ObjectPropertyEditor"
		if (range instanceof BioPAXElement && !model.getObjects().contains(range))
		{
			BioPAXElement bpe = (BioPAXElement) range;
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
    	model.add(element);
	}
}
