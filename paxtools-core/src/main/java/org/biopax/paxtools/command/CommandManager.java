package org.biopax.paxtools.command;

import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;

import java.util.Set;
import java.util.Stack;

/**
 * This is a class for manipulating a BioPAX model via undoable commands
 */

public class CommandManager
{
	private Stack<Command> undoStack = new Stack<Command>();
	private Stack<Command> redoStack = new Stack<Command>();
	private Model model;


	public CommandManager(Model model)
	{
		this.model = model;
	}

	public void addProperty(BioPAXElement bpe, PropertyEditor editor, Object value)
	{
		recordAndRun(editor.isMultipleCardinality() ?
		             new PropertyAddCommand(bpe, editor, value) :
		             new PropertySetCommand(bpe, editor, value));

	}


	public void removeProperty(BioPAXElement bpe, PropertyEditor editor, Object value)
	{
		recordAndRun(new PropertyRemoveCommand(bpe, editor, value));
	}


	public void addObjects(Set<BioPAXElement> bpes)
	{
		recordAndRun(new AddCommand(model, bpes));
	}

	public void removeObjects(Set<BioPAXElement> bpes)
	{
		recordAndRun(new RemoveCommand(model, bpes));
	}

	private void recordAndRun(Command command)
	{
		command.redo();
		undoStack.add(command);
		if (!redoStack.isEmpty())
		{
			redoStack.clear();
		}
	}

	public void undo()
	{
		Command command = undoStack.pop();
		command.undo();
		redoStack.add(command);
	}

	public void redo()
	{
		Command command = redoStack.pop();
		command.redo();
		undoStack.add(command);

	}


	public boolean canUndo()
	{
		return !undoStack.isEmpty();
	}

	public boolean canRedo()
	{
		return !redoStack.isEmpty();
	}
}
