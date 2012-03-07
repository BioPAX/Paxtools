package org.biopax.paxtools.causality.wrapper;

import org.biopax.paxtools.causality.model.Node;
import org.biopax.paxtools.model.level3.Conversion;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class ConversionWrapper extends org.biopax.paxtools.query.wrapperL3.ConversionWrapper
	implements Node
{
	protected ConversionWrapper(Conversion conv, Graph graph)
	{
		super(conv, graph);
	}
}
