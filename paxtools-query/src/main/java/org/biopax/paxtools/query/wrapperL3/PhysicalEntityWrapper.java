package org.biopax.paxtools.query.wrapperL3;

import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.query.model.AbstractNode;
import org.biopax.paxtools.query.model.Edge;
import org.biopax.paxtools.query.model.GraphObject;
import org.biopax.paxtools.query.model.Node;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class PhysicalEntityWrapper extends AbstractNode
{
	protected PhysicalEntity pe;
	protected boolean upperEquivalentInited;
	protected boolean lowerEquivalentInited;
	protected boolean ubique;

	public PhysicalEntityWrapper(PhysicalEntity pe, GraphL3 graph)
	{
		super(graph);
		this.pe = pe;
		this.upperEquivalentInited = false;
		this.lowerEquivalentInited = false;
		this.ubique = false;
	}

	public boolean isUbique()
	{
		return ubique;
	}

	public void setUbique(boolean ubique)
	{
		this.ubique = ubique;
	}

	public void initUpstream()
	{
		for (Conversion conv : getUpstreamConversions(pe.getParticipantOf()))
		{
			ConversionWrapper conW = (ConversionWrapper) graph.getGraphObject(conv);
			if (conv.getConversionDirection() == ConversionDirectionType.REVERSIBLE &&
				conv.getLeft().contains(pe))
			{
				conW = conW.getReverse();
			}
			Edge edge = new EdgeL3(conW, this, graph);
			conW.getDownstreamNoInit().add(edge);
			this.getUpstreamNoInit().add(edge);
		}
	}

	public void initDownstream()
	{
		for (Interaction inter : getDownstreamInteractions(pe.getParticipantOf()))
		{
			AbstractNode node = (AbstractNode) graph.getGraphObject(inter);
			
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

	private Set<Conversion> getUpstreamConversions(Collection<Interaction> inters)
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

	@Override
	public Collection<Node> getUpperEquivalent()
	{
		if (!upperEquivalentInited)
		{
			initUpperEquivalent();
		}
		return super.getUpperEquivalent();
	}

	@Override
	public Collection<Node> getLowerEquivalent()
	{
		if (!lowerEquivalentInited)
		{
			initLowerEquivalent();
		}
		return super.getLowerEquivalent();
	}

	protected void initUpperEquivalent()
	{
		this.upperEquivalent = new HashSet<Node>();

		for (PhysicalEntity eq : pe.getMemberPhysicalEntityOf())
		{
			this.upperEquivalent.add((Node) graph.getGraphObject(eq));
		}

		upperEquivalentInited = true;
	}

	protected void initLowerEquivalent()
	{
		this.lowerEquivalent = new HashSet<Node>();

		for (PhysicalEntity eq : pe.getMemberPhysicalEntity())
		{
			this.lowerEquivalent.add((Node) graph.getGraphObject(eq));
		}

		lowerEquivalentInited = true;
	}

	//------ Other --------------------------------------------------------------------------------|

	public boolean isBreadthNode()
	{
		return true;
	}

	public int getSign()
	{
		return POSITIVE;
	}

	public String getKey()
	{
		return pe.getRDFId();
	}

	public PhysicalEntity getPhysicalEntity()
	{
		return pe;
	}
}
