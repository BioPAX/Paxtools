package org.biopax.paxtools.query.algorithm;

import org.apache.commons.lang3.StringUtils;

/**
 * Direction is used for specifying upstream, downstream or both. Neighborhood and CommonStream
 * queries use this enum as parameter.
 *
 * The difference between BOTHSTREAM and UNDIRECTED: Both-stream means that the linking paths can be
 * either towards upstream or downstream. A linking path will always be directed towards one
 * direction, i.e. the directions of its relations will be consistent. Undirected means the
 * directions are not considered at all, so a linking path can contain relations towards different
 * directions.
 *
 * @author Ozgun Babur
 */
public enum Direction
{
	UPSTREAM("Search direction backwards in the order of events"),
	DOWNSTREAM("Search direction forward in the order of events"),
	BOTHSTREAM("Search towards both directions"),
	UNDIRECTED("Search without considering directions");

	/**
	 * Description of the direction.
	 */
	private final String description;

	/**
	 * Constructor with description.
	 * @param description Description
	 */
	Direction(String description)
	{
		this.description = description;
	}

	/**
	 * Gets the description.
	 * @return description
	 */
	public String getDescription()
	{
		return description;
	}

	public static Direction typeOf(String tag)
	{
		if(StringUtils.isBlank(tag))
			return null;

		Direction type = null;
		try {
			type = valueOf(tag.toUpperCase());
		}
		catch (IllegalArgumentException e){}

		return type;
	}
}
