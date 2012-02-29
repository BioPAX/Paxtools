package org.biopax.paxtools.causality.wrapper;

import org.biopax.paxtools.causality.model.Node;
import org.biopax.paxtools.model.level3.Control;

import java.util.Collections;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class ControlWrapper extends org.biopax.paxtools.query.wrapperL3.ControlWrapper
	implements org.biopax.paxtools.causality.model.Node
{
	protected ControlWrapper(Control ctrl, Graph graph)
	{
		super(ctrl, graph);
	}

	@Override
	public Set<Node> getBanned()
	{
		return Collections.emptySet();
	}
}
