package org.biopax.paxtools.query.algorithm;

import org.biopax.paxtools.query.model.Edge;
import org.biopax.paxtools.query.model.GraphObject;
import org.biopax.paxtools.query.model.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class CycleBreaker extends BFS
{
	Set<GraphObject> result;

	Set<Node> ST;

	public CycleBreaker(Set<GraphObject> result, Set<Node> ST, int limit)
	{
		this.result = result;
		this.ST = ST;
		this.limit = limit;
	}


	public void breakCycles()
	{
		for (GraphObject go : new ArrayList<GraphObject>(result))
		{
			if (go instanceof Node)
			{
				Node node = (Node) go;

				for (Edge edge : node.getDownstream())
				{
					if (result.contains(edge) && !isSafe(node, edge))
					{
						result.remove(edge);
					}
				}
			}
		}
	}


	public boolean isSafe(Node node, Edge edge)
	{
		initMaps();

		setColor(node, BLACK);
		setLabel(node, 0);
		setLabel(edge, 0);

		labelEquivRecursive(node, UPWARD, 0, false, false);
		labelEquivRecursive(node, DOWNWARD, 0, false, false);

		// Initialize dist and color of source set

		Node neigh = edge.getTargetNode();

		if (getColor(neigh) != WHITE) return false;

		setColor(neigh, GRAY);
		setLabel(neigh, 0);

		queue.add(neigh);

		labelEquivRecursive(neigh, UPWARD, 0, true, false);
		labelEquivRecursive(neigh, DOWNWARD, 0, true, false);

		// Process the queue

		while (!queue.isEmpty())
		{
			Node current = queue.remove(0);

			if (ST.contains(current)) return true;
			
			boolean safe = processNode2(current);

			if (safe) return true;

			// Current node is processed
			setColor(current, BLACK);
		}

		return false;
	}


	protected boolean processNode2(Node current)
	{
		return processEdges(current, current.getDownstream()) ||
			processEdges(current, current.getUpstream());
	}

	private boolean processEdges(Node current, Collection<Edge> edges)
	{
		for (Edge edge : edges)
		{
			if (!result.contains(edge)) continue;
			
			// Label the edge considering direction of traversal and type of current node

			setLabel(edge, getLabel(current));

			// Get the other end of the edge
			Node neigh = edge.getSourceNode() == current ?
				edge.getTargetNode() : edge.getSourceNode();

			// Process the neighbor if not processed or not in queue

			if (getColor(neigh) == WHITE)
			{
				// Label the neighbor according to the search direction and node type

				if (neigh.isBreadthNode())
				{
					setLabel(neigh, getLabel(current) + 1);
				}
				else
				{
					setLabel(neigh, getLabel(edge));
				}

				// Check if we need to stop traversing the neighbor, enqueue otherwise

				if (getLabel(neigh) == limit || isEquivalentInTheSet(neigh, ST))
				{
					return true;
				}

				setColor(neigh, GRAY);

				// Enqueue the node according to its type

				if (neigh.isBreadthNode())
				{
					queue.addLast(neigh);
				}
				else
				{
					// Non-breadth nodes are added in front of the queue
					queue.addFirst(neigh);
				}

				labelEquivRecursive(neigh, UPWARD, getLabel(neigh), true, !neigh.isBreadthNode());
				labelEquivRecursive(neigh, DOWNWARD, getLabel(neigh), true, !neigh.isBreadthNode());
			}
		}
		return false;
	}
}
