package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.util.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * A bi-directional BioPAX properties traverser.
 * 
 * To traverse both biopax element's standard properties, such as 'xref',  
 * and (Paxtools') inverse properties, such as 'xrefOf', etc.
 * 
 * @author Ozgun Babur 
 */
public class TraverserBilinked extends Traverser
{
	private boolean isInverseOnly = false;
	private final static Logger log = LoggerFactory.getLogger(TraverserBilinked.class);
	
	/**
	 * Constructor.
	 * 
	 * @param editorMap biopax property editors map
	 * @param visitor user's implementation; if it recursively 
	 * 		calls {@link #traverse(BioPAXElement, Model)} method, then care must be taken to prevent infinite loops.
	 * @param filters bidirectional property filters
	 */
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
		if(!isInverseOnly) {
			super.traverse(element, model);
		}

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
					Set<BioPAXElement> valueSet = new HashSet<>(editor.getInverseAccessor().getValueFromBean(element));
					if (!valueSet.isEmpty()) {
						for (BioPAXElement value : valueSet) {
							if (value != null) {
								//TODO how visitor knows whether it's called from inverse or normal property (e.g., to modify a value)?
								visitor.visit(element, value, model, editor);
							}
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
