package org.biopax.paxtools.query.algorithm;

import org.biopax.paxtools.query.model.Edge;
import org.biopax.paxtools.query.model.GraphObject;
import org.biopax.paxtools.query.model.Node;

import java.util.HashSet;
import java.util.Set;

/**
 * This algorithm is used internally by PathsBetween and PathsFromTo algorithms. It detects objects
 * in the result set that are not on a path from source to target (called dangling), and removes
 * these nodes from the result set. Dangling objects appear after breaking cycles.
 *
 * @author Ozgun Babur
 */
public class Prune
{
	private Set<GraphObject> result;

	/**
	 * Source and targets merged.
	 */
	private Set<Node> ST;


	/**
	 * Constructor with the input.
	 *
	 * @param result The result set
	 * @param ST Source and target nodes
	 */
	public Prune(Set<GraphObject> result, Set<Node> ST)
	{
		this.result = result;
		this.ST = ST;
	}

	/**
	 * Executes the algorithm.
	 * @return the pruned graph
	 */
	public Set<GraphObject> run()
	{
		for (GraphObject go : new HashSet<>(result))
		{
			if (go instanceof Node)
			{
				checkNodeRecursive((Node) go);
			}
		}
		return result;
	}

	/**
	 * Recursively checks if a node is dangling.
	 * @param node Node to check
	 */
	private void checkNodeRecursive(Node node)
	{
		if (isDangling(node))
		{
			removeNode(node);

			for (Edge edge : node.getUpstream())
			{
				checkNodeRecursive(edge.getSourceNode());
			}
			for (Edge edge : node.getDownstream())
			{
				checkNodeRecursive(edge.getTargetNode());
			}
			for (Node parent : node.getUpperEquivalent())
			{
				checkNodeRecursive(parent);
			}
			for (Node child : node.getLowerEquivalent())
			{
				checkNodeRecursive(child);
			}
		}
	}

	/**
	 * Removes the dangling node and its edges.
	 * @param node Node to remove
	 */
	private void removeNode(Node node)
	{
		result.remove(node);

		for (Edge edge : node.getUpstream())
		{
			result.remove(edge);
		}

		for (Edge edge : node.getDownstream())
		{
			result.remove(edge);
		}
	}

	/**
	 * Checks if the node is dangling.
	 * @param node Node to check
	 * @return true if dangling
	 */
	private boolean isDangling(Node node)
	{
		if (!result.contains(node)) return false;
		if (ST.contains(node)) return false;

		boolean hasIncoming = false;

		for (Edge edge : node.getUpstream())
		{
			if (result.contains(edge))
			{
				hasIncoming = true;
				break;
			}
		}

		boolean hasOutgoing = false;

		for (Edge edge : node.getDownstream())
		{
			if (result.contains(edge))
			{
				hasOutgoing = true;
				break;
			}
		}

		if (hasIncoming && hasOutgoing) return false;

		boolean hasParent = false;

		for (Node parent : node.getUpperEquivalent())
		{
			if (result.contains(parent))
			{
				hasParent = true;
				break;
			}
		}

		if (hasParent && (hasIncoming || hasOutgoing)) return false;

		boolean hasChild = false;

		for (Node child : node.getLowerEquivalent())
		{
			if (result.contains(child))
			{
				hasChild = true;
				break;
			}
		}

		return !(hasChild && (hasIncoming || hasOutgoing || hasParent));
	}
}
