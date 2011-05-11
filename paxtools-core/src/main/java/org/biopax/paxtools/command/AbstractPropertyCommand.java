package org.biopax.paxtools.command;

import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;

/**
 */
public abstract class  AbstractPropertyCommand<D extends BioPAXElement,R> implements Command
{
	D bpe;
	PropertyEditor<D,R> editor;
	R value;

	AbstractPropertyCommand(D bpe, PropertyEditor<D,R> editor, R value)
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