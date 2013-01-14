package org.biopax.paxtools.query.model;

/**
 * @author Ozgun Babur
 */
public interface GraphObject
{
	Graph getGraph();

	String getKey();

	/**
	 * This method should clear any analysis specific labels on the object.
	 */
	void clear();
}
