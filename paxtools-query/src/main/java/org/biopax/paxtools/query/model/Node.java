package org.biopax.paxtools.query.model;

import java.util.Collection;

/**
 * This node interface is used in graph algorithms.
 *
 * @author Ozgun Babur
 */
public interface Node extends GraphObject
{
	Collection<Edge> getUpstream();

	Collection<Edge> getDownstream();

	boolean isBreadthNode();

	/**
	 * Abstractions containing this node.
	 */
	Collection<Node> getUpperEquivalent();

	/**
	 * Abstractions or simple nodes, which are also members of this node.
	 */
	Collection<Node> getLowerEquivalent();

	int getSign();

	boolean isUbique();

	void init();

	public static final int POSITIVE = 1;
	public static final int NEGATIVE = -1;
	public static final int NEUTRAL = 0;
}
