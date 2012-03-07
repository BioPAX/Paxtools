package org.biopax.paxtools.query.model;

import java.util.Collection;
import java.util.Collections;
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
	
	protected int pathSign;

	/**
	 * For saying: "If the algorithm traverses this node, it cannot traverse those others". If this
	 * set will be used, then initBanned() should be called. Otherwise getBanned() will return an
	 * immutable empty set.
	 */
	protected Set<Node> banned;

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

	public Set<Node> getBanned()
	{
		if (banned == null) return Collections.emptySet();
		return banned;
	}

	public void initBanned()
	{
		if (banned == null) banned = new HashSet<Node>();
	}

	public void setBanned(Set<Node> banned)
	{
		this.banned = banned;
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

	public int getPathSign()
	{
		return pathSign;
	}

	public void setPathSign(int pathSign)
	{
		this.pathSign = pathSign;
	}
}
