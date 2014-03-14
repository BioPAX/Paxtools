package org.biopax.paxtools.pattern.constraint;

import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.miner.IDFetcher;

import java.util.Set;

/**
 * Checks if the element has a valid ID.
 *
 * @author Ozgun Babur
 */
public class HasAnID extends ConstraintAdapter
{
	/**
	 * ID generator object.
	 */
	private IDFetcher idFetcher;

	/**
	 * Constructor with the ID fetcher.
	 * @param fetcher ID generator
	 */
	public HasAnID(IDFetcher fetcher)
	{
		this.idFetcher = fetcher;
	}

	/**
	 * Returns 1.
	 * @return 1
	 */
	@Override
	public int getVariableSize()
	{
		return 1;
	}

	/**
	 * Checks if the element has one of the desired IDs.
	 * @param match current pattern match
	 * @param ind mapped indices
	 * @return true if the ID is in the list
	 */
	@Override
	public boolean satisfies(Match match, int... ind)
	{
		return idFetcher.fetchID(match.get(ind[0])) != null;
	}
}
