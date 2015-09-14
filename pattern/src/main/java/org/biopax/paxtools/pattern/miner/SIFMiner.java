package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.util.Blacklist;

import java.util.Set;

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
	public SIFType getSIFType();

	/**
	 * Creates SIF interactions for the given match.
	 * @param m match to use for SIF creation
	 * @param fetcher ID generator from BioPAX object
	 * @return SIF interaction
	 */
	public Set<SIFInteraction> createSIFInteraction(Match m, IDFetcher fetcher);

	/**
	 * Sets the blacklist that can be used during the search.
	 * @param blacklist the blacklist
	 */
	public void setBlacklist(Blacklist blacklist);

	/**
	 * Sets the idFetcher that helps to fasten the search.
	 * @param idFetcher ID generator from BioPAX object
	 */
	public void setIDFetcher(IDFetcher idFetcher);
}
