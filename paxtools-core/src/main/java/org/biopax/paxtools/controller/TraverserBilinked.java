package org.biopax.paxtools.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.util.Filter;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Ozgun Babur  //TODO annotate
 */
public class TraverserBilinked extends Traverser
{
	private boolean isInverseOnly = false;
	private final static Log log = LogFactory.getLog(TraverserBilinked.class);
	
	
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
			log.warn("No editors for : " + element.getModelInterface());
			return;
		}
		for (ObjectPropertyEditor editor : editors)
		{
			if (filterInverse(editor))
			{
					Set<BioPAXElement> valueSet = new HashSet(editor.getInverseAccessor().getValueFromBean(element));
					if (!valueSet.isEmpty()) for (BioPAXElement value : valueSet)
					{
						if(value != null) {
							//TODO design issue: how visitor will know whether it's called from inverse or normal property (e.g., to modify value)?
							visitor.visit(element, value, model, editor); 
						}
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
