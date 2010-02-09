package org.biopax.paxtools.controller;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This is a utility class for traversing over the dependent objects of a biopax element, based on
 * property editors
 */
public class Traverser
{
// ------------------------------ FIELDS ------------------------------

	private final EditorMap editorMap;
	private final Visitor visitor;
	private PropertyFilter[] filters;
	protected final Log log = LogFactory.getLog(Traverser.class);


// --------------------------- CONSTRUCTORS ---------------------------

	public Traverser(EditorMap editorMap, Visitor visitor, PropertyFilter... filters)
	{
		this.editorMap = editorMap;
		this.visitor = visitor;
		this.filters = filters;
	}

// -------------------------- OTHER METHODS --------------------------

	/**
	 * Traverse and visit {@link Visitor} all properties of the element. This method does not
	 * traverse iteratively to the values.
	 *
	 * @param element BioPAX element to be traversed
	 * @param model   model containing the graph to be traversed.
	 */
	public void traverse(BioPAXElement element, Model model)
	{
		if (element == null)
		{
			return;
		}

		Set<PropertyEditor> editors =
				editorMap.getEditorsOf(element);

		if(editors == null)
		{
			log.warn("No editors for : " + element.getModelInterface());
			return;
		}
		for (PropertyEditor editor : editors)
		{
			if (filter(editor))
			{

				if (editor.isMultipleCardinality())
				{
					Set valueSet = new HashSet((Collection) editor.getValueFromBean(element));
					for (Object value : valueSet)
					{
						visitor.visit(element, value, model, editor);
					}
				}
				else
				{
					visitor.visit(element, editor.getValueFromBean(element), model, editor);
				}
			}
		}
	}

	private boolean filter(PropertyEditor editor)
	{
		for (PropertyFilter filter : filters)
		{
			if (!filter.filter(editor))
			{
				return false;
			}
		}
		return true;
	}
}

