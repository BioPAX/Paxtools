package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.util.Set;

/**
 */
public class AddCommand extends AbstractAddRemoveCommand
{

	public AddCommand(Model model, Set<BioPAXElement> bpes)
	{
		super(model, bpes);
	}

	@Override
	protected void undoAction(BioPAXElement bpe)
	{
		model.remove(bpe);
	}

	@Override
	protected void redoAction(BioPAXElement bpe)
	{
		model.add(bpe);

	}


	public String getPresentationName()
	{
		return "Add objects to model";
	}

	public String getUndoPresentationName()
	{
		return "Remove objects  from model";
	}

	public String getRedoPresentationName()
	{
		return "Readd objects to the model";
	}


}
