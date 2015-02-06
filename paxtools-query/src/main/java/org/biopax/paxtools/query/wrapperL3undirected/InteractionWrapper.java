package org.biopax.paxtools.query.wrapperL3undirected;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.query.model.AbstractNode;
import org.biopax.paxtools.query.model.Edge;
import org.biopax.paxtools.query.model.Graph;
import org.biopax.paxtools.query.model.Node;

/**
 * Wrapper for Interaction class, excluding Control objects.
 *
 * @author Ozgun Babur
 */
public class InteractionWrapper extends EventWrapper
{
	/**
	 * Wrapped Interaction.
	 */
	private Interaction interaction;

	/**
	 * Constructor with the Interaction and the owner graph.
	 * @param interaction Interaction to wrap
	 * @param graph Owner graph
	 */
	protected InteractionWrapper(Interaction interaction, GraphL3Undirected graph)
	{
		super(graph);

		if (interaction instanceof Control) throw new IllegalArgumentException("Control objects " +
			"should be wrapped with ControlWrapper.");

		this.interaction = interaction;
	}

	/**
	 * Binds to participants and controllers.
	 */
	@Override
	public void initUpstream()
	{
		for (Entity entity : interaction.getParticipant())
		{
			addToUpstream(entity, getGraph());
		}
		for (Control control : interaction.getControlledOf())
		{
			addToUpstream(control, getGraph());
		}
	}

	/**
	 * Binds to participants and controllers.
	 */
	@Override
	public void initDownstream()
	{
		for (Entity entity : interaction.getParticipant())
		{
			addToDownstream(entity, getGraph());
		}
		for (Control control : interaction.getControlledOf())
		{
			addToDownstream(control, getGraph());
		}
	}

	/**
	 * Being a transcription is not relevant in the undirected context.
	 * @return false
	 */
	@Override
	public boolean isTranscription()
	{
		return false;
	}

	/**
	 * Binds the given PhysicalEntity to the downstream.
	 * @param pe PhysicalEntity to bind
	 * @param graph Owner graph
	 */
	protected void addToDownstream(BioPAXElement pe, Graph graph)
	{
		AbstractNode node = (AbstractNode) graph.getGraphObject(pe);

		if (node != null)
		{
			EdgeL3 edge = new EdgeL3(this, node, graph);
			edge.setTranscription(true);

			node.getUpstreamNoInit().add(edge);
			this.getDownstreamNoInit().add(edge);
		}
	}

	/**
	 * Binds the given element to the upstream.
	 * @param ele Element to bind
	 * @param graph Owner graph
	 */
	protected void addToUpstream(BioPAXElement ele, org.biopax.paxtools.query.model.Graph graph)
	{
		AbstractNode node = (AbstractNode) graph.getGraphObject(ele);
		if (node != null)
		{
			Edge edge = new EdgeL3(node, this, graph);

			node.getDownstreamNoInit().add(edge);
			this.getUpstreamNoInit().add(edge);
		}
	}

	/**
	 * Uses RDF ID of TemplateReaction as key.
	 * @return RDF ID of TemplateReaction
	 */
	public String getKey()
	{
		return interaction.getRDFId();
	}

	/**
	 * Gets the wrapped TemplateReaction
	 * @return The wrapped TemplateReaction
	 */
	public Interaction getInteraction()
	{
		return interaction;
	}
}
