package org.biopax.paxtools.query.wrapperL3undirected;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.query.model.AbstractNode;
import org.biopax.paxtools.query.model.Edge;
import org.biopax.paxtools.query.model.Graph;
import org.biopax.paxtools.query.model.Node;

import java.util.Collection;
import java.util.Collections;

/**
 * This is the parent wrapper class for both Conversion and TemplateReaction objects.
 *
 * @author Ozgun Babur
 */
public abstract class EventWrapper extends AbstractNode
{
	/**
	 * Constructor with the owner graph.
	 * @param graph Owner graph
	 */
	protected EventWrapper(GraphL3Undirected graph)
	{
		super(graph);
	}

	/**
	 * Events are not breadth nodes.
	 * @return False
	 */
	public boolean isBreadthNode()
	{
		return false;
	}

	/**
	 * Events have a positive sign.
	 * @return POSITIVE (1)
	 */
	public int getSign()
	{
		return POSITIVE;
	}

	/**
	 * Events are not ubiquitous molecules.
	 * @return False
	 */
	public boolean isUbique()
	{
		return false;
	}

	/**
	 * Say if the event is a transcription.
	 * @return Whether the event is a transcription
	 */
	public abstract boolean isTranscription();

	/**
	 * Bind the wrapper of the given element to the upstream.
	 * @param ele Element to bind
	 * @param graph Owner graph.
	 */
	protected void addToUpstream(BioPAXElement ele, Graph graph)
	{
		AbstractNode node = (AbstractNode) graph.getGraphObject(ele);
		if (node == null) return;

		Edge edge = new EdgeL3(node, this, graph);
		
		if (isTranscription())
		{
			if (node instanceof ControlWrapper)
			{
				((ControlWrapper) node).setTranscription(true);
			}
		}
		
		node.getDownstreamNoInit().add(edge);
		this.getUpstreamNoInit().add(edge);
	}

	/**
	 * Bind the wrapper of the given PhysicalEntity to the downstream.
	 * @param pe PhysicalEntity to bind
	 * @param graph Owner graph.
	 */
	protected void addToDownstream(PhysicalEntity pe, Graph graph)
	{
		AbstractNode node = (AbstractNode) graph.getGraphObject(pe);
		if (node == null) return;

		Edge edge = new EdgeL3(this, node, graph);

		node.getUpstreamNoInit().add(edge);
		this.getDownstreamNoInit().add(edge);
	}

	/**
	 * Events do not have equivalent objects.
	 * @return Empty set
	 */
	@Override
	public Collection<Node> getUpperEquivalent()
	{
		return Collections.emptySet();
	}

	/**
	 * Events do not have equivalent objects.
	 * @return Empty set
	 */
	@Override
	public Collection<Node> getLowerEquivalent()
	{
		return Collections.emptySet();
	}
}
