package org.biopax.paxtools.command;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;

import java.util.Set;

/**
 */
public class RemoveCommand extends AbstractAddRemoveCommand
{

	public RemoveCommand(Model model, Set<BioPAXElement> bpes)
	{
		super(model, bpes);
	}

	@Override
	protected void undoAction(BioPAXElement bpe)
	{
		model.add(bpe);
	}

	@Override
	protected void redoAction(BioPAXElement bpe)
	{
		model.remove(bpe);
	}


	public String getPresentationName()
	{
		return "Remove objects from model";
	}

	public String getUndoPresentationName()
	{
		return "Add objects to model";
	}

	public String getRedoPresentationName()
	{
		return "Re-remove objects from model";
	}



}
