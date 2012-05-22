package org.biopax.paxtools.causality.wrapper;

import org.biopax.paxtools.causality.model.AlterationPack;
import org.biopax.paxtools.causality.model.Node;
import org.biopax.paxtools.model.level3.*;

import java.util.*;

/**
 * @author Ozgun Babur
 */
public class PhysicalEntityWrapper extends org.biopax.paxtools.query.wrapperL3.PhysicalEntityWrapper
	implements Node
{
	protected List<Xref> xrefs;

	AlterationPack alterations;

	public PhysicalEntityWrapper(PhysicalEntity pe, Graph graph)
	{
		super(pe, graph);
	}

	@Override
	protected void initLowerEquivalent()
	{
		super.initLowerEquivalent();

		initComplexMemberEqs();
	}

	protected void initComplexMemberEqs()
	{
		if (pe instanceof Complex)
		{
			for (PhysicalEntity memPE : ((Complex) pe).getComponent())
			{
				ComplexMember mem = ((Graph) graph).getMember(memPE);
				lowerEquivalent.add(mem);
			}
		}
	}

	public List<Xref> getXRefs()
	{
		if (xrefs == null)
		{
			xrefs = new ArrayList<Xref>();
			xrefs.addAll(pe.getXref());
			if (pe instanceof SimplePhysicalEntity)
			{
				SimplePhysicalEntity spe = (SimplePhysicalEntity) pe;
				EntityReference er = spe.getEntityReference();
				if (er != null) xrefs.addAll(er.getXref());
			}
		}
		return xrefs;
	}

	protected Set<Interaction> getDownstreamInteractions(Collection<Interaction> inters)
	{
		Set<Interaction> set = new HashSet<Interaction>();

		for (Interaction inter : inters)
		{
			if (inter instanceof Control)
			{
				set.add(inter);
			}
		}
		return set;
	}

	public AlterationPack getAlterations()
	{
		if (alterations == null)
		{
			alterations = getGraph().getAlterationProvider().getAlterations(this);
		}
		return alterations;
	}

	public void setAlterations(AlterationPack pack)
	{
		this.alterations = pack;
	}
	
	public Graph getGraph()
	{
		return (Graph) super.getGraph();
	}

	@Override
	public String toString()
	{
		return pe.getDisplayName();
	}
}
