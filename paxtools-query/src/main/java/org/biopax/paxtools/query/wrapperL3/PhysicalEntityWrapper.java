package org.biopax.paxtools.query.wrapperL3;

import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.query.model.AbstractNode;
import org.biopax.paxtools.query.model.Edge;
import org.biopax.paxtools.query.model.Node;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
	public PhysicalEntityWrapper(PhysicalEntity pe, GraphL3 graph)
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
		for (Conversion conv : getUpstreamConversions(pe.getParticipantOf()))
		{
			ConversionWrapper conW = (ConversionWrapper) graph.getGraphObject(conv);
			if (conW == null) continue;

			if (conv.getConversionDirection() == ConversionDirectionType.REVERSIBLE &&
				conv.getLeft().contains(pe))
			{
				conW = conW.getReverse();
			}
			Edge edge = new EdgeL3(conW, this, graph);
			conW.getDownstreamNoInit().add(edge);
			this.getUpstreamNoInit().add(edge);
		}

		for (Interaction inter : pe.getParticipantOf())
		{
			if (inter instanceof TemplateReaction)
			{
				TemplateReaction tr = (TemplateReaction) inter;
				TemplateReactionWrapper trw = (TemplateReactionWrapper) graph.getGraphObject(tr);
				if (trw == null) continue;

				Edge edge = new EdgeL3(trw, this, graph);

				assert trw.getDownstreamNoInit() != null;

				trw.getDownstreamNoInit().add(edge);
				this.getUpstreamNoInit().add(edge);
			}
		}
	}

	/**
	 * Binds to downstream interactions.
	 */
	public void initDownstream()
	{
		for (Interaction inter : getDownstreamInteractions(pe.getParticipantOf()))
		{
			AbstractNode node = (AbstractNode) graph.getGraphObject(inter);

			if (node == null) continue;

			if (inter instanceof Conversion)
			{
				Conversion conv = (Conversion) inter;
				ConversionWrapper conW = (ConversionWrapper) node;
				if (conv.getConversionDirection() == ConversionDirectionType.REVERSIBLE &&
					conv.getRight().contains(pe))
				{
					node = conW.getReverse();
				}
			}

			Edge edge = new EdgeL3(this, node, graph);
			this.getDownstreamNoInit().add(edge);
			node.getUpstreamNoInit().add(edge);
		}
	}

	//--- Upstream conversions --------------------------------------------------------------------|

	/**
	 * Gets the conversions at the upstream of this PhysicalEntity.
	 * @param inters Interactions to search for
	 * @return Upstream conversions
	 */
	protected Set<Conversion> getUpstreamConversions(Collection<Interaction> inters)
	{
		Set<Conversion> set = new HashSet<Conversion>();

		for (Interaction inter : inters)
		{
			if (inter instanceof Conversion)
			{
				Conversion conv = (Conversion) inter;
				ConversionDirectionType dir = conv.getConversionDirection();

				if (dir == ConversionDirectionType.REVERSIBLE ||
					(dir == ConversionDirectionType.RIGHT_TO_LEFT && conv.getLeft().contains(pe)) ||
					((dir == ConversionDirectionType.LEFT_TO_RIGHT || dir == null) &&
						conv.getRight().contains(pe)))
				{
					set.add(conv);
				}
			}
		}
		return set;
	}

	//--- Downstream interactions ------------------------------------------------------------------|

	/**
	 * Gets the downstream interactions among the given set.
	 * @param inters Interactions to search
	 * @return Downstream interactions
	 */
	protected Set<Interaction> getDownstreamInteractions(Collection<Interaction> inters)
	{
		Set<Interaction> set = new HashSet<Interaction>();

		for (Interaction inter : inters)
		{
			if (inter instanceof Conversion)
			{
				Conversion conv = (Conversion) inter;
				ConversionDirectionType dir = conv.getConversionDirection();

				if (dir == ConversionDirectionType.REVERSIBLE ||
					(dir == ConversionDirectionType.RIGHT_TO_LEFT && conv.getRight().contains(pe)) ||
					((dir == ConversionDirectionType.LEFT_TO_RIGHT || dir == null) &&
						conv.getLeft().contains(pe)))
				{
					set.add(conv);
				}
			}
			else if (inter instanceof Control)
			{
				set.add(inter);
			}
		}
		return set;
	}

	//--- Related conversions ---------------------------------------------------------------------|

	/**
	 * Get all related Conversions of the given Interaction set.
	 * @param inters Interactions to query
	 * @return Related Conversions
	 */
	private Set<Conversion> getRelatedConversions(Collection<Interaction> inters)
	{
		Set<Conversion> set = new HashSet<Conversion>();

		for (Interaction inter : inters)
		{
			if (inter instanceof Conversion)
			{
				set.add((Conversion) inter);
			}
			else if (inter instanceof Control)
			{
				getRelatedConversions((Control) inter, set);
			}
		}
		return set;
	}

	/**
	 * Recursively searches the related Conversions of a Control.
	 * @param ctrl Control to query
	 * @param set Set to collect the related Conversions
	 * @return The same set
	 */
	private Set<Conversion> getRelatedConversions(Control ctrl, Set<Conversion> set)
	{
		for (Process process : ctrl.getControlled())
		{
			if (process instanceof Conversion)
			{
				set.add((Conversion) process);
			}
			else if (process instanceof Control)
			{
				getRelatedConversions((Control) process, set);
			}
		}
		return set;
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
		this.upperEquivalent = new HashSet<Node>();

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
		this.lowerEquivalent = new HashSet<Node>();

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
