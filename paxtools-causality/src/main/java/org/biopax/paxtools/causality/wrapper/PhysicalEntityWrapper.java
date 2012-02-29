package org.biopax.paxtools.causality.wrapper;

import org.biopax.paxtools.causality.model.Node;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.query.wrapperL3.GraphL3;

import java.util.Collections;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class PhysicalEntityWrapper extends org.biopax.paxtools.query.wrapperL3.PhysicalEntityWrapper
	implements Node
{
	public PhysicalEntityWrapper(PhysicalEntity pe, Graph graph)
	{
		super(pe, graph);
	}

	@Override
	public Set<Node> getBanned()
	{
		return Collections.emptySet();
	}
}
