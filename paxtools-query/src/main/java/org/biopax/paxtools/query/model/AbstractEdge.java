package org.biopax.paxtools.query.model;

/**
 * @author Ozgun Babur
 */
public class AbstractEdge implements Edge
{
	private Node source;
	private Node target;
	private Graph graph;

	public AbstractEdge(Node source, Node target, Graph graph)
	{
		this.source = source;
		this.target = target;
		this.graph = graph;
	}

	public Node getTargetNode()
	{
		return target;
	}

	public Node getSourceNode()
	{
		return source;
	}

	public Graph getGraph()
	{
		return graph;
	}

	public String getKey()
	{
		return source.getKey() + "|" + target.getKey();
	}
}
