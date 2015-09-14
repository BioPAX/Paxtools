package org.biopax.paxtools.pattern.constraint;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.miner.IDFetcher;

import java.util.Map;
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

	private Map<BioPAXElement, Set<String>> idMap;

	/**
	 * Constructor with the ID fetcher.
	 * @param fetcher ID generator
	 * @param idMap map of IDs
	 */
	public HasAnID(IDFetcher fetcher, Map<BioPAXElement, Set<String>> idMap)
	{
		this.idFetcher = fetcher;
		this.idMap = idMap;
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
		BioPAXElement ele = match.get(ind[0]);
		if (!idMap.containsKey(ele)) idMap.put(ele, idFetcher.fetchID(ele));
		return idMap.get(ele) != null && !idMap.get(ele).isEmpty();
	}
}
