package org.biopax.paxtools.command;

import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;

public class PropertyAddCommand extends AbstractPropertyCommand
{

	PropertyAddCommand(BioPAXElement bpe, PropertyEditor editor, Object value)
	{
		super(bpe, editor, value);
		if (!editor.isMultipleCardinality())
		{
			throw new IllegalArgumentException();
		}
	}



	@Override
	protected void undoAction()
	{
		editor.removeValueFromBean(value, bpe);
	}

	public boolean canUndo()
	{
		return bpe != null;
	}


	@Override
	protected void redoAction()
	{
		editor.setValueToBean(value, bpe);
	}

	public boolean canRedo()
	{
		return bpe != null && value != null;
	}

	public boolean isSignificant()
	{
		return value != null;
	}

	public String getPresentationName()
	{
		return "Add " + value + " to " + editor.getProperty() + "s of " + bpe;
	}

	public String getUndoPresentationName()
	{
		return "Remove" + value + "from" + editor.getProperty() + "s of " + bpe;
	}

	public String getRedoPresentationName()
	{
		return "Re-Add " + value + " to " + editor.getProperty() + "s of " + bpe;
	}
}
