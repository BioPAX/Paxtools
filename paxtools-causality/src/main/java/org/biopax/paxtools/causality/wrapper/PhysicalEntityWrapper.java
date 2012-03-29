package org.biopax.paxtools.causality.wrapper;

import org.biopax.paxtools.causality.model.AlterationPack;
import org.biopax.paxtools.causality.model.Node;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.model.level3.Xref;

import java.util.ArrayList;
import java.util.List;

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
}
