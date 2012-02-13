package org.biopax.paxtools.query.algorithm;

/**
 * Specifies whether the length limit is a normal limit or shortest_plus_k limit. PathsFromToQuery
 * use this as a parameter.
 *
 * @author Ozgun Babur
 */
public enum LimitType
{
	NORMAL,
	SHORTEST_PLUS_K
}
