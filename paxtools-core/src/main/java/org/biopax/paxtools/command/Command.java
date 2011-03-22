package org.biopax.paxtools.command;

public interface Command
{
	public boolean undo();

	public boolean redo();
}
