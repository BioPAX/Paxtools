package org.biopax.paxtools.query.wrapperL3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.query.model.AbstractNode;
import org.biopax.paxtools.query.model.Edge;
import org.biopax.paxtools.query.model.Graph;
import org.biopax.paxtools.query.model.Node;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Ozgun Babur
 */
public class TemplateReactionWrapper extends EventWrapper
{
	private TemplateReaction tempReac;

	protected TemplateReactionWrapper(TemplateReaction tempReac, GraphL3 graph)
	{
		super(graph);
		this.tempReac = tempReac;
	}

	@Override
	public void initUpstream()
	{
		NucleicAcid nuc = tempReac.getTemplate();
		if (nuc != null)
		addToUpstream(nuc, getGraph());

		for (Control cont : tempReac.getControlledOf())
		{
			addToUpstream(cont, graph);
		}
	}

	@Override
	public void initDownstream()
	{
		for (PhysicalEntity pe : tempReac.getProduct())
		{
			addToDownstream(pe, getGraph());
		}
	}

	protected void addToDownstream(PhysicalEntity pe, Graph graph)
	{
		AbstractNode node = (AbstractNode) graph.getGraphObject(pe);
		EdgeL3 edge = new EdgeL3(this, node, graph);
		edge.setTranscription(true);

		node.getUpstreamNoInit().add(edge);
		this.getDownstreamNoInit().add(edge);
	}


	public String getKey()
	{
		return tempReac.getRDFId();
	}

	public TemplateReaction getTempReac()
	{
		return tempReac;
	}
}
