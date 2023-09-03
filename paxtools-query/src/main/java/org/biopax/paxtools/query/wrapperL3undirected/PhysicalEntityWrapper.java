package org.biopax.paxtools.query.wrapperL3undirected;

import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.query.model.AbstractNode;
import org.biopax.paxtools.query.model.Edge;
import org.biopax.paxtools.query.model.Node;

import java.util.Collection;
import java.util.HashSet;

/**
 * Wrapper for PhysicalEntity.
 *
 * @author Ozgun Babur
 */
public class PhysicalEntityWrapper extends AbstractNode
{
	/**
	 * Wrapped PhysicalEntity.
	 */
	protected PhysicalEntity pe;

	/**
	 * Flag to remember if parent equivalents initialized.
	 */
	protected boolean upperEquivalentInited;

	/**
	 * Flag to remember if child equivalents initialized.
	 */
	protected boolean lowerEquivalentInited;

	/**
	 * Flag to say this is a ubiquitous molecule.
	 */
	protected boolean ubique;

	/**
	 * Constructor with the wrapped PhysicalEntity and the owner graph.
	 * @param pe PhysicalEntity to wrap
	 * @param graph Owner graph
	 */
	public PhysicalEntityWrapper(PhysicalEntity pe, GraphL3Undirected graph)
	{
		super(graph);
		this.pe = pe;
		this.upperEquivalentInited = false;
		this.lowerEquivalentInited = false;
		this.ubique = false;
	}

	/**
	 * @return Whether this is ubique
	 */
	public boolean isUbique()
	{
		return ubique;
	}

	/**
	 * Set the ubique flag.
	 * @param ubique Whether this is a ubiquitous molecule
	 */
	public void setUbique(boolean ubique)
	{
		this.ubique = ubique;
	}

	/**
	 * Binds to upstream interactions.
	 */
	public void initUpstream()
	{
		for (Interaction inter : pe.getParticipantOf())
		{
			AbstractNode wrapper = (AbstractNode) graph.getGraphObject(inter);

			if (wrapper == null) continue;

			Edge edge = new EdgeL3(wrapper, this, graph);
			wrapper.getDownstreamNoInit().add(edge);
			this.getUpstreamNoInit().add(edge);
		}
	}

	/**
	 * Binds to downstream interactions.
	 */
	public void initDownstream()
	{
		for (Interaction inter : pe.getParticipantOf())
		{
			AbstractNode wrapper = (AbstractNode) graph.getGraphObject(inter);

			if (wrapper == null) continue;

			Edge edge = new EdgeL3(this, wrapper, graph);
			this.getDownstreamNoInit().add(edge);
			wrapper.getUpstreamNoInit().add(edge);
		}
	}

	//----- Equivalence ---------------------------------------------------------------------------|

	/**
	 * @return Parent equivalent objects
	 */
	@Override
	public Collection<Node> getUpperEquivalent()
	{
		if (!upperEquivalentInited)
		{
			initUpperEquivalent();
		}
		return super.getUpperEquivalent();
	}

	/**
	 * @return Child equivalent objects
	 */
	@Override
	public Collection<Node> getLowerEquivalent()
	{
		if (!lowerEquivalentInited)
		{
			initLowerEquivalent();
		}
		return super.getLowerEquivalent();
	}

	/**
	 * Finds homology parent.
	 */
	protected void initUpperEquivalent()
	{
		this.upperEquivalent = new HashSet<>();

		for (PhysicalEntity eq : pe.getMemberPhysicalEntityOf())
		{
			Node node = (Node) graph.getGraphObject(eq);
			if (node != null) this.upperEquivalent.add(node);
		}

		upperEquivalentInited = true;
	}

	/**
	 * Finds member nodes if this is a homology node
	 */
	protected void initLowerEquivalent()
	{
		this.lowerEquivalent = new HashSet<>();

		for (PhysicalEntity eq : pe.getMemberPhysicalEntity())
		{
			Node node = (Node) graph.getGraphObject(eq);
			if (node != null) this.lowerEquivalent.add(node);
		}

		lowerEquivalentInited = true;
	}

	//------ Other --------------------------------------------------------------------------------|

	/**
	 * PhysicalEntity is a breadth node.
	 * @return True
	 */
	public boolean isBreadthNode()
	{
		return true;
	}

	/**
	 * PhysicalEntity have positive sign.
	 * @return POSITIVE (1)
	 */
	public int getSign()
	{
		return POSITIVE;
	}

	/**
	 * RDF ID of the PhysicalEntity is used as key.
	 * @return Key
	 */
	public String getKey()
	{
		return pe.getUri();
	}

	/**
	 * @return Wrapped PhysicalEntity
	 */
	public PhysicalEntity getPhysicalEntity()
	{
		return pe;
	}

	/**
	 * @return display name with ID added
	 */
	@Override
	public String toString()
	{
		return pe.getDisplayName() + " -- "+ pe.getUri();
	}
}
