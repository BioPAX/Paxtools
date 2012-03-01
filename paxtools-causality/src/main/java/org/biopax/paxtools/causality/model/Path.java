package org.biopax.paxtools.causality.model;

import org.biopax.paxtools.query.model.Edge;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * This a signed and directed path.
 *
 * @author Ozgun Babur
 */
public class Path
{
	protected LinkedList<Node> nodes;
	protected LinkedList<Edge> edges;
	
	protected boolean reverse;
	
	protected int sign;
	
	protected int length;

	Set<Node> nodeSet = new HashSet<Node>();

	protected PathUser user;

	public Path(Node initial)
	{
		this(initial, null);
	}

	public Path(Node initial, PathUser user)
	{
		nodes = new LinkedList<Node>();
		edges = new LinkedList<Edge>();
		nodes.add(initial);
		nodeSet.add(initial);
		reverse = false;
		sign = 1;
		length = 0;
		this.user = user;
	}

	public int getSign()
	{
		return sign;
	}

	public boolean isReverse()
	{
		return reverse;
	}

	public void setReverse(boolean reverse)
	{
		this.reverse = reverse;
	}

	public void add(Node node, Edge edge)
	{
		if (nodeSet.contains(node))
		{
			throw new IllegalArgumentException("The node is already on the path. Node: " + node);
		}
		if (edge != null)
		{
			// Assert that the path here is also a path in the original network
			if ((!reverse && edge.getSourceNode() != nodes.getLast()) ||
				(reverse && edge.getTargetNode() != nodes.getLast()))
				throw new IllegalArgumentException("The edge is not leading to or coming from the" +
					" last node in the current list.");
		}

		nodes.add(node);
		edges.add(edge);

		nodeSet.add(node);
		sign *= node.getSign();
		if (edge != null) sign *= edge.getSign();

		if (node.isBreadthNode() && edge != null) length++;

		// Notify the user for the addition to the path
		if (user != null) user.processPath(this);
	}
	
	public void removeLast()
	{
		Node node = nodes.removeLast();
		Edge edge = edges.removeLast();
		nodeSet.remove(node);
		sign *= node.getSign();
		if (edge != null) sign *= edge.getSign();

		if (node.isBreadthNode() && edge != null) length--;
	}

	public boolean contains(Node node)
	{
		return nodeSet.contains(node);
	}
	
	public boolean canAdd(Node node)
	{
		if (nodeSet.contains(node)) return false;

		for (Node n : nodes)
		{
			if (n.getBanned().contains(node)) return false;
		}
		return true;
	}

	public Node getLastNode()
	{
		return nodes.getLast();
	}
	
	public Edge getLastEdge()
	{
		return edges.getLast();
	}
	
	public int getNodeSize()
	{
		return nodes.size();
	}

	public int getLength()
	{
		return length;
	}
}
