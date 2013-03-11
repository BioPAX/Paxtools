package org.biopax.paxtools.controller;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.util.Filter;

import java.util.HashSet;
import java.util.Set;

/**
 * This is a utility class for traversing over the dependent objects of a biopax element, based on
 * property editors
 */
public class Traverser
{
// ------------------------------ FIELDS ------------------------------

	protected final EditorMap editorMap;

	protected Visitor visitor;

	protected Filter<PropertyEditor>[] filters;

	private final static Log log = LogFactory.getLog(Traverser.class);


// --------------------------- CONSTRUCTORS ---------------------------

	public Traverser(EditorMap editorMap, Visitor visitor, Filter<PropertyEditor>... filters)
	{
		this.editorMap = editorMap;
		this.visitor = visitor;
		this.filters = filters;
	}

// -------------------------- SETTERS/GETTERS ------------------------

	public void setVisitor(Visitor visitor)
	{
		this.visitor = visitor;
	}

	public Visitor getVisitor()
	{
		return visitor;
	}

// -------------------------- OTHER METHODS --------------------------

	/**
	 * Traverse and visit {@link Visitor} all properties of the element.
	 * This method does not traverse iteratively to the values.
	 * @param element BioPAX element to be traversed
	 * @param model to be traversed, but not necessarily (depends on the Visitor implementation).
	 * @param <D> actual BioPAX type which properties and inherited properties will be used
	 */
	public <D extends BioPAXElement> void traverse(D element, Model model)
	{
		if (element == null)
		{
			return;
		}

		Set<PropertyEditor> editors = editorMap.getEditorsOf(element);

		if (editors == null)
		{
			log.warn("No editors for : " + element.getModelInterface());
			return;
		}
		for (PropertyEditor<? super D,?> editor : editors)
		{
			if (filter(editor))
			{
				Set<?> valueSet = new HashSet<Object>(editor.getValueFromBean(element));
				if (!valueSet.isEmpty())
                    traverseElements(element, model, editor, valueSet);
            }
		}
	}

    protected void traverseElements(BioPAXElement element, Model model, PropertyEditor<?,?> editor, Set<?> valueSet)
    {
        for (Object value : valueSet)
        {
        	visitor.visit(element, value, model, editor);
        }
    }


    protected boolean filter(PropertyEditor<? extends BioPAXElement,?> editor)
	{
		for (Filter<PropertyEditor> filter : filters)
		{
			if (!filter.filter(editor))
			{
				return false;
			}
		}
		return true;
	}
}

