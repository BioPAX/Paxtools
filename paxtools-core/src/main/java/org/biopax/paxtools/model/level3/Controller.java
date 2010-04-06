package org.biopax.paxtools.model.level3;

import java.util.Set;

/**
 *  Interface for pathway elements that can control processes.
 */
public interface Controller extends Entity
{
	public Set<Control> getControllerOf();
}
