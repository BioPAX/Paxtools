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
	/**
	 * Owner graph.
	 */
	protected Graph graph;

	/**
	 * Flag to remember if upstream links were created.
	 */
	protected boolean upstreamInited;

	/**
	 * Flag to remember if downstream links were created.
	 */
	protected boolean downstreamInited;

	/**
	 * If there are equivalent nodes in the graph, and they have a hierarchy, like an homology node
	 * and members, then this set is for the parent equivalents of this node.
	 */
	protected Set<Node> upperEquivalent;

	/**
	 * If there are equivalent nodes in the graph, and they have a hierarchy, like an homology node
	 * and members, then this set is for the child equivalents of this node.
	 */
	protected Set<Node> lowerEquivalent;

	/**
	 * Set of upstream edges.
	 */
	protected Set<Edge> upstream;

	/**
	 * Set of downstream edges.
	 */
	protected Set<Edge> downstream;

	/**
	 * This variable can be used by algorithms that need to label nodes with a path sign
	 * (typically the current path).
	 */
	protected int pathSign;

	/**
	 * For saying: "If the algorithm traverses this node, it cannot traverse those others". If this
	 * set will be used, then initBanned() should be called. Otherwise getBanned() will return an
	 * immutable empty set.
	 */
	protected Set<Node> banned;

	/**
	 * Constructor with the owner graph.
	 * @param graph Owner graph
	 */
	protected AbstractNode(Graph graph)
	{
		this.graph = graph;
		this.upstream = new HashSet<Edge>();
		this.downstream = new HashSet<Edge>();
		this.upstreamInited = false;
		this.downstreamInited = false;
	}

	/**
	 * @return The owner graph
	 */
	public Graph getGraph()
	{
		return graph;
	}

	/**
	 * @return Set of banned-to-traverse nodes if this node is traversed.
	 */
	public Set<Node> getBanned()
	{
		if (banned == null) return Collections.emptySet();
		return banned;
	}

	/**
	 * Initializes the set of banned nodes. If the algorithm will use this set, then this method
	 * or setBanned method should be called.
	 */
	public void initBanned()
	{
		if (banned == null) banned = new HashSet<Node>();
	}

	/**
	 * @param banned Set of banned nodes
	 */
	public void setBanned(Set<Node> banned)
	{
		this.banned = banned;
	}

	/**
	 * Gets the upstream edges. Initializes if necessary.
	 * @return upstream edges
	 */
	public Collection<Edge> getUpstream()
	{
		if (!upstreamInited)
		{
			initUpstream();
			upstreamInited = true;
		}
		return upstream;
	}

	/**
	 * Gets the downstream edges. Initializes if necessary.
	 * @return downstream edges
	 */
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

	/**
	 * Initializes the upstream connections.
	 */
	public void initUpstream(){}

	/**
	 * Initializes the downstream connections.
	 */
	public void initDownstream(){}

	/**
	 * This class gets the upstream links but does not initialize.
	 * @return Upstream links
	 */
	public Collection<Edge> getUpstreamNoInit()
	{
		return upstream;
	}

	/**
	 * This class gets the downstream links but does not initialize.
	 * @return Downstream links
	 */
	public Collection<Edge> getDownstreamNoInit()
	{
		return downstream;
	}

	/**
	 * @return Parent equivalent nodes.
	 */
	public Collection<Node> getUpperEquivalent()
	{
		return upperEquivalent;
	}

	/**
	 * @return Child equivalent nodes
	 */
	public Collection<Node> getLowerEquivalent()
	{
		return lowerEquivalent;
	}

	/**
	 * Does nothing yet.
	 */
	public void init()
	{
	}

	/**
	 * @return path sign
	 */
	public int getPathSign()
	{
		return pathSign;
	}

	/**
	 * @param pathSign The path sign
	 */
	public void setPathSign(int pathSign)
	{
		this.pathSign = pathSign;
	}

	/**
	 * Resets the path sign.
	 */
	public void clear()
	{
		this.pathSign = 0;
	}

	/**
	 * Nodes are not transcription by default.
	 * @return False
	 */
	@Override
	public boolean isTranscription()
	{
		return false;
	}
}
