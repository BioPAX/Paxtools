package org.biopax.paxtools.query.algorithm;

/**
 * Direction is used for specifying upstream, downstream or both. Neighborhood and CommonStream
 * queries use this enum as parameter.
 *
 * @author Ozgun Babur
 */
public enum Direction
{
	UPSTREAM,
	DOWNSTREAM,
	BOTHSTREAM
}
