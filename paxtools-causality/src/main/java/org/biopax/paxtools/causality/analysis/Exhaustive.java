package org.biopax.paxtools.causality.analysis;

import org.biopax.paxtools.causality.model.Node;
import org.biopax.paxtools.causality.model.Path;
import org.biopax.paxtools.causality.model.PathUser;
import org.biopax.paxtools.query.algorithm.Direction;
import org.biopax.paxtools.query.model.Edge;

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

	public Exhaustive(Node source, Direction direction, int limit, PathUser user)
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
			Node neigh = (Node) (direction == Direction.DOWNSTREAM ?
				edge.getTargetNode() : edge.getSourceNode());

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
