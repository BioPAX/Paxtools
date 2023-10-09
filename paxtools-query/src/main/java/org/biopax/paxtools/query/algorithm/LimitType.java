package org.biopax.paxtools.query.algorithm;

import org.apache.commons.lang3.StringUtils;

/**
 * Specifies whether the length limit is a normal limit or shortest_plus_k limit. PathsFromToQuery
 * use this as a parameter.
 *
 * @author Ozgun Babur
 */
public enum LimitType
{
	NORMAL,
	SHORTEST_PLUS_K;

	public static LimitType typeOf(String tag)
	{
		if(StringUtils.isBlank(tag))
			return null;

		LimitType type = null;
		try {
			tag = tag.toUpperCase().replaceAll("-", "_");
			type = valueOf(tag);
		}
		catch (IllegalArgumentException e){}

		return type;
	}
}
