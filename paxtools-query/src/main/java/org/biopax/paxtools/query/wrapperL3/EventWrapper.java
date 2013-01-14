package org.biopax.paxtools.query.wrapperL3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.query.model.AbstractNode;
import org.biopax.paxtools.query.model.Edge;
import org.biopax.paxtools.query.model.Graph;
import org.biopax.paxtools.query.model.Node;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Ozgun Babur
 */
public abstract class EventWrapper extends AbstractNode
{
	protected EventWrapper(GraphL3 graph)
	{
		super(graph);
	}

	public boolean isBreadthNode()
	{
		return false;
	}

	public int getSign()
	{
		return POSITIVE;
	}

	public boolean isUbique()
	{
		return false;
	}

	public abstract boolean isTranscription();
	
	protected void addToUpstream(BioPAXElement ele, Graph graph)
	{
		AbstractNode node = (AbstractNode) graph.getGraphObject(ele);
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

	protected void addToDownstream(PhysicalEntity pe, Graph graph)
	{
		AbstractNode node = (AbstractNode) graph.getGraphObject(pe);
		Edge edge = new EdgeL3(this, node, graph);

		node.getUpstreamNoInit().add(edge);
		this.getDownstreamNoInit().add(edge);
	}

	@Override
	public Collection<Node> getUpperEquivalent()
	{
		return Collections.emptySet();
	}

	@Override
	public Collection<Node> getLowerEquivalent()
	{
		return Collections.emptySet();
	}
}
