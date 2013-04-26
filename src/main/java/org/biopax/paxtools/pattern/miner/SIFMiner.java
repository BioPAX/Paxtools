package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.pattern.Match;

/**
 * This interface tells that the miner supports SIF output.
 * @author Ozgun Babur
 */
public interface SIFMiner extends Miner
{
	/**
	 * Gets the label of the source node.
	 * @return source label
	 */
	public String getSourceLabel();

	/**
	 * Gets the label of the target node.
	 * @return target label
	 */
	public String getTargetLabel();

	/**
	 * Gets the type of the interaction.
	 * @return interaction type
	 */
	public String getRelationType(Match m);

	/**
	 * Tells if the mined interactions are directed.
	 * @return true if directed
	 */
	public boolean isDirected();

	/**
	 * Creates a SIF interaction for the given match.
	 * @param m match to use for SIF creation
	 * @return SIF interaction
	 */
	public SIFInteraction createSIFInteraction(Match m);
}
