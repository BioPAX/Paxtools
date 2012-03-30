package org.biopax.paxtools.causality.wrapper;

import org.biopax.paxtools.causality.model.AlterationPack;
import org.biopax.paxtools.causality.model.Node;
import org.biopax.paxtools.model.level3.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class PhysicalEntityWrapper extends org.biopax.paxtools.query.wrapperL3.PhysicalEntityWrapper
	implements Node
{
	protected List<Xref> xrefs;
	protected Set<ComplexMember> members;

	AlterationPack alterations;

	public PhysicalEntityWrapper(PhysicalEntity pe, Graph graph)
	{
		super(pe, graph);
	}

	@Override
	public void init()
	{
		super.init();
		
		if (pe instanceof Complex)
		{
			members = new HashSet<ComplexMember>();

			for (PhysicalEntity memPE : ((Complex) pe).getComponent())
			{
				ComplexMember mem = ((Graph) graph).getMember(memPE);
				mem.addComplex(this);
				this.addComplexMember(mem);
			}
		}
	}

	public void addComplexMember(ComplexMember mem)
	{
		members.add(mem);
		if (lowerEquivalentInited) lowerEquivalent.add(mem);
	}

	@Override
	protected void initLowerEquivalent()
	{
		super.initLowerEquivalent();
		if (members != null) lowerEquivalent.addAll(members);
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
