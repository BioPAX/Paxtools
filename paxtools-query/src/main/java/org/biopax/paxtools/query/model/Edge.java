package org.biopax.paxtools.query.model;

/**
 * Wrapper interface for edges in a graph to be queried.
 *
 * @author Ozgun Babur
 */
public interface Edge extends GraphObject
{
	/**
	 * @return The target node
	 */
	Node getTargetNode();

	/**
	 * @return The source node
	 */
	Node getSourceNode();

	/**
	 * Algorithms may need a sign for the edge. 1: positive, -1 negative, 0: signless.
	 * @return The sign of the edge
	 */
	public int getSign();

	/**
	 * @return Whether this edge indicates a transcriptional relation
	 */
	public boolean isTranscription();
}
