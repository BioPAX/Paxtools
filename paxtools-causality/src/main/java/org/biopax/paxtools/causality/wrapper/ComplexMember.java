package org.biopax.paxtools.causality.wrapper;

import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.query.model.Node;

import java.util.HashSet;

/**
 * @author Ozgun Babur
 */
public class ComplexMember extends PhysicalEntityWrapper
{
	public ComplexMember(PhysicalEntity pe, Graph graph)
	{
		super(pe, graph);
	}

	@Override
	public void init()
	{
		super.init();
	}

	@Override
	public void initUpstream()
	{
		upstreamInited = true;
	}

	@Override
	public void initDownstream()
	{
		downstreamInited = true;
	}

	@Override
	protected void initUpperEquivalent()
	{
		upperEquivalent = new HashSet<Node>();
		for (Complex cmp : pe.getComponentOf())
		{
			PhysicalEntityWrapper pew = (PhysicalEntityWrapper) graph.getGraphObject(cmp);
			upperEquivalent.add(pew);
		}

		if (!pe.getMemberPhysicalEntityOf().isEmpty())
		{
			for (PhysicalEntity upe : pe.getMemberPhysicalEntityOf())
			{
				ComplexMember umem = ((Graph) graph).getMember(upe);
				upperEquivalent.add(umem);
			}
		}

		upperEquivalentInited = true;
	}

	@Override
	protected void initLowerEquivalent()
	{
		lowerEquivalent = new HashSet<Node>();

		initComplexMemberEqs();

		if (!pe.getMemberPhysicalEntity().isEmpty())
		{
			for (PhysicalEntity lpe : pe.getMemberPhysicalEntity())
			{
				ComplexMember lmem = ((Graph) graph).getMember(lpe);
				lowerEquivalent.add(lmem);
			}
		}

		lowerEquivalentInited = true;
	}
}
