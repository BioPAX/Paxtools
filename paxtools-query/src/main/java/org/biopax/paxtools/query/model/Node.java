package org.biopax.paxtools.query.model;

import java.util.Collection;

/**
 * Node interface to use in graph algorithms.
 *
 * @author Ozgun Babur
 */
public interface Node extends GraphObject
{
	/**
	 * @return Upstream edges
	 */
	Collection<Edge> getUpstream();

	/**
	 * @return Downstream edges
	 */
	Collection<Edge> getDownstream();

	/**
	 * This method is critical when the algorithm needs to calculate a path length. A graph may
	 * contain nodes that will not add to the path length. Such nodes are not breadth node. So the
	 * length of the path is the number of breadth nodes - 1.
	 *
	 * @return Whether this is a breadth node
	 */
	boolean isBreadthNode();

	/**
	 * @return Parent equivalent nodes
	 */
	Collection<Node> getUpperEquivalent();

	/**
	 * @return Child equivalent nodes
	 */
	Collection<Node> getLowerEquivalent();

	/**
	 * Some nodes can have a sign (typically non-breadth nodes).
	 * @return Sign of the node
	 */
	int getSign();

	/**
	 * In biological graphs, some nodes are used ubiquitously like ATP, H2O, etc. While traversing a
	 * graph we do not want these molecules to link two reactions. So they should be labeled.
	 * @return Whether this note is a ubiquitous node.
	 */
	boolean isUbique();

	/**
	 * A node may be related to a transcription and an algorithm can depend on this information.
	 * @return Whether this node is related to a transcription event
	 */
	boolean isTranscription();

	/**
	 * Initializes the node.
	 */
	void init();

	/**
	 * Positive sign. This is not an Enum because we want to be able calculate sign of a path by
	 * multiplying signs of path elements.
	 */
	public static final int POSITIVE = 1;

	/**
	 * Negative sign. This is not an Enum because we want to be able calculate sign of a path by
	 * multiplying signs of path elements.
	 */
	public static final int NEGATIVE = -1;

	/**
	 * Neutral sign. This is not an Enum because we want to be able calculate sign of a path by
	 * multiplying signs of path elements.
	 */
	public static final int NEUTRAL = 0;
}
