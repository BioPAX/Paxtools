package org.biopax.paxtools.io.sbgn;

import org.biopax.paxtools.model.level3.Entity;

import java.util.Set;

/**
 * Detects ubiquitous molecules using a given ID set.
 *
 * @author Ozgun Babur
 */
public class ListUbiqueDetector implements UbiqueDetector
{
	Set<String> uris;

	/**
	 * Contructor.
	 *
	 * @param uris URIs of ubiques
	 */
	public ListUbiqueDetector(Set<String> uris)
	{
		this.uris = uris;
	}

	/**
	 * Checks if the ID of the PhysicalEntity is in the set.
	 * @param e PhysicalEntity or Gene to check
	 * @return true if ubique
	 */
	public boolean isUbique(Entity e)
	{
		return uris.contains(e.getUri());
	}
}
