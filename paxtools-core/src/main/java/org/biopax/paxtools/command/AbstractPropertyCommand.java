package org.biopax.paxtools.command;

import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;

/**
 */
public abstract class AbstractPropertyCommand implements Command
{
	BioPAXElement bpe;
	PropertyEditor editor;
	Object value;

	AbstractPropertyCommand(BioPAXElement bpe, PropertyEditor editor, Object value)
	{

		if (bpe == null || editor == null)
		{
			throw new IllegalArgumentException();
		}
		this.bpe = bpe;
		this.editor = editor;
		this.value=value;

	}

	public boolean undo()
	{
		try
		{
			undoAction();
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	protected abstract void undoAction();


	public boolean redo()
	{
		try
		{
			redoAction();
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	protected abstract void redoAction();

}