package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.util.Filter;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class TraverserBilinked extends Traverser
{
	public TraverserBilinked(EditorMap editorMap, Visitor visitor, PropertyFilterBilinked... filters)
	{
		super(editorMap, visitor, filters);
	}

	@Override
	public void traverse(BioPAXElement element, Model model)
	{
		super.traverse(element, model);

		// Now traverse the inverse links

		Set<ObjectPropertyEditor> editors = editorMap.getInverseEditorsOf(element);

		if(editors == null)
		{
			if(log.isWarnEnabled())
				log.warn("No editors for : " + element.getModelInterface());
			return;
		}
		for (ObjectPropertyEditor editor : editors)
		{
			if (filterInverse(editor))
			{
				if (editor.isInverseMultipleCardinality())
				{
					Set valueSet = new HashSet(editor.getInverseAccessor().getValueFromBean(element));
					for (Object value : valueSet)
					{
						visitor.visit(element, value, model, editor);
					}
				}
				else
				{
					visitor.visit(element, editor.getInverseAccessor().getValueFromBean(element), model, editor);
				}
			}
		}
	}

	protected boolean filterInverse(PropertyEditor editor)
	{
		for (Filter<PropertyEditor> filter : filters)
		{
			if (!((PropertyFilterBilinked) filter).filterInverse(editor))
			{
				return false;
			}
		}
		return true;
	}

}
