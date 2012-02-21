package org.biopax.paxtools.query.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public abstract class AbstractNode implements Node
{
	protected Graph graph;

	protected boolean upstreamInited;
	protected boolean downstreamInited;

	protected Set<Node> upperEquivalent;
	protected Set<Node> lowerEquivalent;

	protected Set<Edge> upstream;
	protected Set<Edge> downstream;

	protected AbstractNode(Graph graph)
	{
		this.graph = graph;
		this.upstream = new HashSet<Edge>();
		this.downstream = new HashSet<Edge>();
		this.upstreamInited = false;
		this.downstreamInited = false;
	}

	public Graph getGraph()
	{
		return graph;
	}

	public Collection<Edge> getUpstream()
	{
		if (!upstreamInited)
		{
			initUpstream();
			upstreamInited = true;
		}
		return upstream;
	}

	public Collection<Edge> getDownstream()
	{
		if (!downstreamInited)
		{
			initDownstream();
			downstreamInited = true;
		}
		return downstream;
	}

	// These two methods should be overriden if any upstream and downstream initing is required.

	public void initUpstream(){}
	public void initDownstream(){}

	/**
	 * This class gets the upstream links but does not initialize
	 * @return
	 */
	public Collection<Edge> getUpstreamNoInit()
	{
		return upstream;
	}

	public Collection<Edge> getDownstreamNoInit()
	{
		return downstream;
	}

	public Collection<Node> getUpperEquivalent()
	{
		return upperEquivalent;
	}

	public Collection<Node> getLowerEquivalent()
	{
		return lowerEquivalent;
	}

	/**
	 * Assumes the node has no need to be initialized.
	 */
	public void init()
	{
	}
}
