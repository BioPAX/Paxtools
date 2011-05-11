package org.biopax.paxtools.controller;

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
    public Fetcher(EditorMap editorMap, Filter<PropertyEditor>... filters) {
        super(editorMap, filters);
    }

    /**
     * Adds the BioPAX element into the model and traverses the element
     * for its dependent elements.
     */
    @Override
    protected void visit(Object range, BioPAXElement domain, Model model, PropertyEditor editor)
	{
		if (range instanceof BioPAXElement && !model.contains((BioPAXElement) range))
		{
			BioPAXElement bpe = (BioPAXElement) range;
			model.add(bpe);
			super.traverse(bpe, model);
		}
	}

    /**
     * Adds the element and all its children to the model.
     *
     * @param element the BioPAX element to be added into the model
     * @param model model into which elements will be added
     */
    public void fetch(BioPAXElement element, Model model)
	{
    	super.traverse(element, model);
        model.add(element);
	}
}
