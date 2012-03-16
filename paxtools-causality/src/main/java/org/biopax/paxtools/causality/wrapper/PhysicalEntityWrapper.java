package org.biopax.paxtools.causality.wrapper;

import org.biopax.paxtools.causality.model.Node;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.model.level3.Xref;
import org.biopax.paxtools.query.wrapperL3.GraphL3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class PhysicalEntityWrapper extends org.biopax.paxtools.query.wrapperL3.PhysicalEntityWrapper
	implements Node
{
	protected List<Xref> xrefs;

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
}
