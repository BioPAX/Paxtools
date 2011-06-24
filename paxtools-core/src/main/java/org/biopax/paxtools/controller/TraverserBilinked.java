package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.util.Filter;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class TraverserBilinked extends Traverser
{
	private boolean isInverseOnly = false;
	
	
	public TraverserBilinked(EditorMap editorMap, Visitor visitor, PropertyFilterBilinked... filters)
	{
		super(editorMap, visitor, filters);
	}

	public boolean isInverseOnly() {
		return isInverseOnly;
	}
	public void setInverseOnly(boolean isInverseOnly) {
		this.isInverseOnly = isInverseOnly;
	}
	
	@Override
	public void traverse(BioPAXElement element, Model model)
	{
		if(!isInverseOnly)
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
					Set valueSet = new HashSet(editor.getInverseAccessor().getValueFromBean(element));
					if (!valueSet.isEmpty()) for (Object value : valueSet)
					{
						if(value != null)
							visitor.visit(element, value, model, editor);
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
