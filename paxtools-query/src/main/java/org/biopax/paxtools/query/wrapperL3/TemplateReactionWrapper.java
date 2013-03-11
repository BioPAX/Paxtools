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
 * Wrapper for TemplateReaction class.
 *
 * @author Ozgun Babur
 */
public class TemplateReactionWrapper extends EventWrapper
{
	/**
	 * Wrapped TemplateReaction.
	 */
	private TemplateReaction tempReac;

	/**
	 * Constructor with the TemplateReaction and the owner graph.
	 * @param tempReac TemplateReaction to wrap
	 * @param graph Owner graph
	 */
	protected TemplateReactionWrapper(TemplateReaction tempReac, GraphL3 graph)
	{
		super(graph);
		this.tempReac = tempReac;
	}

	/**
	 * Binds to template and controllers.
	 */
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

	/**
	 * Binds to products.
	 */
	@Override
	public void initDownstream()
	{
		for (PhysicalEntity pe : tempReac.getProduct())
		{
			addToDownstream(pe, getGraph());
		}
	}

	/**
	 * This is transcription.
	 * @return True
	 */
	@Override
	public boolean isTranscription()
	{
		return true;
	}

	/**
	 * Binds the given PhysicalEntity to the downstream.
	 * @param pe PhysicalEntity to bind
	 * @param graph Owner graph
	 */
	protected void addToDownstream(PhysicalEntity pe, Graph graph)
	{
		AbstractNode node = (AbstractNode) graph.getGraphObject(pe);
		EdgeL3 edge = new EdgeL3(this, node, graph);
		edge.setTranscription(true);

		node.getUpstreamNoInit().add(edge);
		this.getDownstreamNoInit().add(edge);
	}

	/**
	 * Binds the given element to the upstream.
	 * @param ele Element to bind
	 * @param graph Owner graph
	 */
	protected void addToUpstream(BioPAXElement ele, org.biopax.paxtools.query.model.Graph graph)
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

	/**
	 * Uses RDF ID of TemplateReaction as key.
	 * @return RDF ID of TemplateReaction
	 */
	public String getKey()
	{
		return tempReac.getRDFId();
	}

	/**
	 * Gets the wrapped TemplateReaction
	 * @return The wrapped TemplateReaction
	 */
	public TemplateReaction getTempReac()
	{
		return tempReac;
	}
}
