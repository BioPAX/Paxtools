package org.biopax.paxtools.query.utilL3;

import org.biopax.paxtools.model.level3.Level3Element;

import java.util.Set;

/**
 * Filters out ubiquitous entities, whose IDs are supplied by user.
 *
 * @author Ozgun Babur
 */
public class UbiqueFilter extends Filter
{
	/**
	 * IDs of ubiquitous entities.
	 */
	private Set<String> ubiqueIDs;

	/**
	 * Constructor.
	 * @param ubiqueIDs IDS of ubiquitous entities
	 */
	public UbiqueFilter(Set<String> ubiqueIDs)
	{
		this.ubiqueIDs = ubiqueIDs;
	}

	/**
	 * Checks if the ID of the given element is in the black list.
	 * @param ele level 3 element to check
	 * @return true if the element is not black-listed
	 */
	@Override
	public boolean okToTraverse(Level3Element ele)
	{
		return !ubiqueIDs.contains(ele.getRDFId());
	}
}
