package org.biopax.paxtools.causality.analysis;

import org.biopax.paxtools.causality.model.Node;
import org.biopax.paxtools.causality.model.Path;
import org.biopax.paxtools.causality.model.PathUser;
import org.biopax.paxtools.query.algorithm.Direction;
import org.biopax.paxtools.query.model.Edge;
import org.biopax.paxtools.query.model.GraphObject;

import java.util.Set;

/**
 * Performs an exhaustive search from a single source node.
 *
 * @author Ozgun Babur
 */
public class Exhaustive
{
	protected Path path;
	
	Node source;
	int limit;
	Direction direction;

	/**
	 * If the user provides this set, then the exhaustive search will traverse only the objects
	 * which are also in this set. This is useful for for first finding a relation between nodes
	 * using an efficient algorithm, then generating the path between with exhaustive search over
	 * labelled objects.
	 */
	Set<GraphObject> limitingSet;

	/**
	 * Tells if the limiting set also limits traversing edges. If the limiting set contains only the
	 * nodes, then this flag should be false. Otherwise the search won't advance.
	 */
	boolean limitEdgesToo;

	public Exhaustive(Node source, Direction direction, int limit, PathUser user)
	{
		this(source, direction, limit, user, null);
	}

	public Exhaustive(Node source, Direction direction, int limit, PathUser user,
		Set<GraphObject> limitingSet)
	{
		if (direction == Direction.BOTHSTREAM)
		{
			throw new IllegalArgumentException("Cannot perform exhaustive search to both sides. " +
				"Please select either UPSTREAM or DOWNSTREAM.");
		}

		this.source = source;
		this.limit = limit;
		this.direction = direction;

		path = new Path(source, user);
		if (direction == Direction.UPSTREAM) path.setReverse(true);

		this.limitingSet = limitingSet;
		this.limitEdgesToo = true;
	}

	public boolean isLimitEdgesToo()
	{
		return limitEdgesToo;
	}

	public void setLimitEdgesToo(boolean limitEdgesToo)
	{
		this.limitEdgesToo = limitEdgesToo;
	}

	public void run()
	{
		advanceRecursive();
	}
	
	protected void advanceRecursive()
	{
		Node node = path.getLastNode();
		
		if (path.getNodeSize() == 1 || path.getLastEdge() != null)
		{
			processEquivalents(true);
			processEquivalents(false);
		}

		if (path.getLength() == limit) return;

		for (Edge edge : direction == Direction.DOWNSTREAM ? 
			node.getDownstream() : node.getUpstream())
		{
			// If the edge is where we are not supposed to go, don't go.
			if (limitEdgesToo && limitingSet != null && !limitingSet.contains(edge)) continue;

			Node neigh = (Node) (direction == Direction.DOWNSTREAM ?
				edge.getTargetNode() : edge.getSourceNode());

			// If the node is where we are not supposed to go, don't go.
			if (limitingSet != null && !limitingSet.contains(node)) continue;

			if (path.canAdd(neigh))
			{
				path.add(neigh, edge);

				advanceRecursive();

				path.removeLast();
			}
		}
	}

	/**
	 * Another recursive method for traversing equivalent nodes.
	 * @param upper
	 */
	protected void processEquivalents(boolean upper)
	{
		Node node = path.getLastNode();

		for (org.biopax.paxtools.query.model.Node equivalent : upper ? 
			node.getUpperEquivalent() : node.getLowerEquivalent())
		{
			// If the node is where we are not supposed to go, don't go.
			if (limitingSet != null && !limitingSet.contains(equivalent)) continue;

			Node eq = (Node) equivalent;
			
			if (path.canAdd(eq))
			{
				path.add(eq, null);
	
				advanceRecursive();
	
				processEquivalents(upper);
	
				path.removeLast();
			}
		}
	}
}
