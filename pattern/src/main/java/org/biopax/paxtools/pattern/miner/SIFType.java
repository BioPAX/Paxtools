package org.biopax.paxtools.pattern.miner;

import java.util.List;

/**
 * @author Ozgun Babur
 */
public interface SIFType
{
	/**
	 * Tag of a SIF type is derived from the enum name.
	 * @return tag
	 */
	public String getTag();

	/**
	 * Asks if the edge is directed.
	 * @return true if directed
	 */
	public boolean isDirected();

	/**
	 * Gets the description of the SIF type.
	 * @return description
	 */
	public String getDescription();

	/**
	 * Gets the list of SIFMiner classes that can search this type of SIF edge in a BioPAX model.
	 * @return SIF miners
	 */
	public List<Class<? extends SIFMiner>> getMiners();
}
