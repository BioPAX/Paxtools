package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;

/**
 * @author Ozgun Babur
 */
public interface IDFetcher
{
	/**
	 * Finds a String ID for the given element.
	 * @param ele element to fecth the ID from
	 * @return ID
	 */
	public String fetchID(BioPAXElement ele);
}
