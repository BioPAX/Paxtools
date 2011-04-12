package org.biopax.paxtools.model.level3;

import java.util.Set;

/**
 * This is a role interface for pathway elements that can control processes, namely {@link Pathway} and {@link
 * PhysicalEntity}.
 */
public interface Controller extends Entity
{
	/**
	 * This method returns the list of {@link Control} interactions that this entity controls. Reverse method of
	 * {@link Control#getController()}. Contents of this set is automatically maintained and should not be modified.
	 * @return list of {@link Control} interactions that this entity controls.
	 */
	public Set<Control> getControllerOf();
}
