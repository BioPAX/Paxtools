package org.biopax.paxtools.model.level2;

import java.util.Set;

/**
 * marker interface for entities and PEPs
 */
public interface InteractionParticipant extends Level2Element
{
// -------------------------- OTHER METHODS --------------------------

	/**
	 * This method  returns the interaction that this entity/pep takes part in.
	 * Contents of this set should not be modified.
	 *
	 * @return a set of interactions that
	 */
	public Set<interaction> isPARTICIPANTSof();
}
