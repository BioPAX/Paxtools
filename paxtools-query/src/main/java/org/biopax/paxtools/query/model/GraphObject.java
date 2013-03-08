package org.biopax.paxtools.query.model;

/**
 * A graph object is the common interface for nodes and edges in a graph.
 *
 * @author Ozgun Babur
 */
public interface GraphObject
{
	/**
	 * Every graph object should have an owner graph.
	 *
	 * @return The owner graph
	 */
	Graph getGraph();

	/**
	 * This method is used for storing the object in a Map. The key should be uniquely generated for
	 * each edge in a graph.
	 *
	 * @return The key
	 */
	String getKey();

	/**
	 * This method should clear any analysis specific labels on the object.
	 */
	void clear();
}
