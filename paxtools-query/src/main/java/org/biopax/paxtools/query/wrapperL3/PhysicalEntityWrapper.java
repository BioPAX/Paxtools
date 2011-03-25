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
 * @author Ozgun Babur
 */
public class PhysicalEntityWrapper extends AbstractNode
{
	PhysicalEntity pe;
	boolean interactionsInited;
	boolean equivalentInited;

	public PhysicalEntityWrapper(PhysicalEntity pe, GraphL3 graph)
	{
		super(graph);
		this.pe = pe;
		this.interactionsInited = false;
		this.equivalentInited = false;
	}

	@Override
	public Collection<Edge> getUpstream()
	{
		if (!interactionsInited)
		{
			initInteractions();
		}
		return super.getUpstream();
	}

	@Override
	public Collection<Edge> getDownstream()
	{
		if (!interactionsInited)
		{
			initInteractions();
		}
		return super.getDownstream();
	}

	protected void initInteractions()
	{
		for (Conversion conv : getRelatedConversions(pe.getParticipantOf()))
		{
			graph.getGraphObject(conv);
		}
		
		interactionsInited = true;
	}

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

	@Override
	public Collection<Node> getUpperEquivalent()
	{
		if (!equivalentInited)
		{
			initEquivalent();
		}
		return super.getUpperEquivalent();
	}

	@Override
	public Collection<Node> getLowerEquivalent()
	{
		if (!equivalentInited)
		{
			initEquivalent();
		}
		return super.getLowerEquivalent();
	}

	protected void initEquivalent()
	{
		this.upperEquivalent = new HashSet<Node>();
		this.lowerEquivalent = new HashSet<Node>();
		collectUpperEquivalent(pe);
		collectLowerEquivalent(pe);
		equivalentInited = true;
	}

	protected void collectUpperEquivalent(PhysicalEntity pe)
	{
		for (PhysicalEntity eq : pe.getMemberPhysicalEntityOf())
		{
			this.upperEquivalent.add((Node) graph.getGraphObject(eq));
		}

//		for (PhysicalEntity eq : pe.getComponentOf())
//		{
//			this.upperEquivalent.add((Node) graph.getGraphObject(eq));
//		}
	}
	
	protected void collectLowerEquivalent(PhysicalEntity pe)
	{
		for (PhysicalEntity eq : pe.getMemberPhysicalEntity())
		{
			this.lowerEquivalent.add((Node) graph.getGraphObject(eq));
		}

//		if (pe instanceof Complex)
//		{
//			for (PhysicalEntity eq : ((Complex) pe).getComponent())
//			{
//				this.lowerEquivalent.add((Node) graph.getGraphObject(eq));
//			}
//		}
	}

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
