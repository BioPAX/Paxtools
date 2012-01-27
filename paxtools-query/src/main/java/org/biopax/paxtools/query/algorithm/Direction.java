package org.biopax.paxtools.query.algorithm;

/**
 * Direction is used for specifying upstream, downstream or both. Neighborhood and CommonStream
 * queries use this enum as parameter.
 *
 * @author Ozgun Babur
 */
public enum Direction
{
	UPSTREAM("Search direction backwards in the order of events"),
	DOWNSTREAM("Search direction forward in the order of events"),
	BOTHSTREAM("Search towards both directions");

	private final String description;

	private Direction(String description)
	{
		this.description = description;
	}

	public String getDescription()
	{
		return description;
	}
}
