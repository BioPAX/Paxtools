package org.biopax.paxtools.command;

import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;

public class PropertySetCommand extends AbstractPropertyCommand
{
	private Object oldValue;

	PropertySetCommand(BioPAXElement bpe, PropertyEditor editor, Object value)
	{
		super(bpe, editor, value);
		if (!editor.isMultipleCardinality())
		{
			throw new IllegalArgumentException();
		}
		oldValue = editor.getValueFromBean(bpe);
	}


	@Override
	protected void undoAction()
	{
		editor.setValueToBean(oldValue, bpe);

	}



	@Override
	protected void redoAction()
	{
		editor.setValueToBean(value, bpe);
	}


	public String getPresentationName()
	{
		return "Set " + editor.getProperty() + " of " + bpe + " to " + value;
	}

	public String getUndoPresentationName()
	{
		return "Set " + editor.getProperty() + " of " + bpe + " to " + oldValue;
	}

	public String getRedoPresentationName()
	{
		return "Set " + editor.getProperty() + " of " + bpe + " to " + value;
	}
}
