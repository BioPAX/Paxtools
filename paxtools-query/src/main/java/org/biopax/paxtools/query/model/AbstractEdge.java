package org.biopax.paxtools.query.model;

/**
 * The base class for an edge in the traversed graph.
 *
 * @author Ozgun Babur
 */
public abstract class AbstractEdge implements Edge
{
	private Node source;
	private Node target;
	private Graph graph;

	/**
	 * Edges should know their source and target nodes, and their graph.
	 *
	 * @param source
	 * @param target
	 * @param graph
	 */
	public AbstractEdge(Node source, Node target, Graph graph)
	{
		this.source = source;
		this.target = target;
		this.graph = graph;
	}

	/**
	 * @return The target node
	 */
	public Node getTargetNode()
	{
		return target;
	}

	/**
	 * @return The source node
	 */
	public Node getSourceNode()
	{
		return source;
	}

	/**
	 * @return The owner graph
	 */
	public Graph getGraph()
	{
		return graph;
	}

	/**
	 * @return Key to use in a map
	 */
	public String getKey()
	{
		return source.getKey() + "|" + target.getKey();
	}

	@Override
	public int hashCode()
	{
		return source.hashCode() + target.hashCode() + graph.hashCode();
	}

	@Override
	/**
	 * Two edges are equal if source, target and owner graphs are identical.
	 */
	public boolean equals(Object obj)
	{
		if (obj instanceof AbstractEdge)
		{
			AbstractEdge e = (AbstractEdge) obj;
			return source == e.getSourceNode() &&
				target == e.getTargetNode() &&
				graph == e.getGraph();
		}
		return false;
	}

	/**
	 * Edges are positive by default.
	 * @return 1 indicating a positive edge
	 */
	@Override
	public int getSign()
	{
		return 1;
	}

	/**
	 * Does nothing yet.
	 */
	public void clear()
	{
	}
}
