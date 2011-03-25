package org.biopax.paxtools.command;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;

import java.util.Set;


public abstract class AbstractAddRemoveCommand implements Command
{
	Model model;
	Set<BioPAXElement> bpes;

	public AbstractAddRemoveCommand(Model model, Set<BioPAXElement> bpes)
	{
		this.model = model;
		this.bpes = bpes;
	}

	public boolean undo()
	{
		try
		{
			for (BioPAXElement bpe : bpes)
			{
				undoAction(bpe);
			}
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	protected abstract void undoAction(BioPAXElement bpe);

	public boolean canUndo()
	{
		return true;
	}

	public boolean redo()
	{
		try
		{
			for (BioPAXElement bpe : bpes)
			{
				redoAction(bpe);
			}
			return true;
			}
		catch (Exception e)
		{
			return false;
		}
	}

	protected abstract void redoAction(BioPAXElement bpe);
}
