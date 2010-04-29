package org.biopax.paxtools.query.model;

import java.util.Collection;

/**
 * This node interface is used in graph algorithms.
 *
 * @author Ozgun Babur
 */
public interface Node extends GraphObject
{
	public Collection<Edge> getUpstream();

	public Collection<Edge> getDownstream();

	public boolean isBreadthNode();

	/**
	 * Abstractions containing this node.
	 */
	public Collection<Node> getUpperEquivalent();

	/**
	 * Abstractions or simple nodes, which are also members of this node.
	 */
	public Collection<Node> getLowerEquivalent();

	public int getSign();

	public void init();

	public static final int POSITIVE = 1;
	public static final int NEGATIVE = -1;
	public static final int NEUTRAL = 0;
}
