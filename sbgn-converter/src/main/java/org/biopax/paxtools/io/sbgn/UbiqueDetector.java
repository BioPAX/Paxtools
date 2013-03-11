package org.biopax.paxtools.io.sbgn;

import org.biopax.paxtools.model.level3.PhysicalEntity;

/**
 * Interface for ubiquitous molecule detection.
 *
 * @author Ozgun Babur
 */
public interface UbiqueDetector
{
	/**
	 * Checks if the PhysicalEntity is a ubiquitous molecule.
	 * @param pe PhysicalEntity to check
	 * @return true if ubique
	 */
	public boolean isUbique(PhysicalEntity pe);
}
