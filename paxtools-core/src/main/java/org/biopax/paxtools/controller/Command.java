package org.biopax.paxtools.controller;

public interface Command
{
	public boolean undo();

	public boolean redo();
}
