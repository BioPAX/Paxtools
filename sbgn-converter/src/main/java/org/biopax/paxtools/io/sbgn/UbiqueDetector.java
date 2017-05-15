package org.biopax.paxtools.io.sbgn;

import org.biopax.paxtools.model.level3.Entity;

/**
 * Interface for ubiquitous molecule detection.
 *
 * @author Ozgun Babur
 */
public interface UbiqueDetector
{
	/**
	 * Checks if the PhysicalEntity is a ubiquitous molecule.
	 * @param e PhysicalEntity or Gene to check
	 * @return true if ubique
	 */
	boolean isUbique(Entity e);
}
