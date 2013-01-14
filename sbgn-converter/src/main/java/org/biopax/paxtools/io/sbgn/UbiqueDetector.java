package org.biopax.paxtools.io.sbgn;

import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.PhysicalEntity;

/**
 * Interface for ubiquitous molecule detection
 *
 * @author Ozgun Babur
 */
public interface UbiqueDetector
{
	public boolean isUbique(PhysicalEntity pe);
}
