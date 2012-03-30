package org.biopax.paxtools.causality.wrapper;

import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.query.model.Node;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class ComplexMember extends PhysicalEntityWrapper
{
	protected Set<PhysicalEntityWrapper> complexes;
	protected Set<ComplexMember> upperEq;
	protected Set<ComplexMember> lowerEq;

	public ComplexMember(PhysicalEntity pe, Graph graph)
	{
		super(pe, graph);
		complexes = new HashSet<PhysicalEntityWrapper>();
	}

	public void addComplex(PhysicalEntityWrapper pew)
	{
		complexes.add(pew);
		if (upperEquivalentInited) upperEquivalent.add(pew);
	}

	@Override
	public void init()
	{
		super.init();
		for (Complex cmp : pe.getComponentOf())
		{
			PhysicalEntityWrapper pew = (PhysicalEntityWrapper) graph.getGraphObject(cmp);
			complexes.add(pew);
			pew.addComplexMember(this);
		}
		if (!pe.getMemberPhysicalEntityOf().isEmpty())
		{
			upperEq = new HashSet<ComplexMember>();
			for (PhysicalEntity upe : pe.getMemberPhysicalEntityOf())
			{
				ComplexMember umem = ((Graph) graph).getMember(upe);
				upperEq.add(umem);
			}
		}
		if (!pe.getMemberPhysicalEntity().isEmpty())
		{
			lowerEq = new HashSet<ComplexMember>();
			for (PhysicalEntity lpe : pe.getMemberPhysicalEntity())
			{
				ComplexMember lmem = ((Graph) graph).getMember(lpe);
				lowerEq.add(lmem);
			}
		}
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
		upperEquivalent.addAll(complexes);
		if (upperEq != null) upperEquivalent.addAll(upperEq);

		upperEquivalentInited = true;
	}

	@Override
	protected void initLowerEquivalent()
	{
		lowerEquivalent = new HashSet<Node>();
		if (members != null) lowerEquivalent.addAll(members);
		if (lowerEq != null) lowerEquivalent.addAll(lowerEq);


		lowerEquivalentInited = true;
	}
}
