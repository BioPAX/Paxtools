package org.biopax.paxtools.causality.wrapper;

import org.biopax.paxtools.causality.model.Node;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.query.model.AbstractNode;

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

	@Override
	public void initDownstream()
	{
		for (org.biopax.paxtools.model.level3.Process prc : ctrl.getControlled())
		{
			if (prc instanceof Conversion || prc instanceof Control)
			{
				AbstractNode node = (AbstractNode) graph.getGraphObject(prc);
				Edge edge = new Edge(this, node, (Graph) graph);
				edge.setSign(this.getSign());
				node.getUpstreamNoInit().add(edge);
				getDownstreamNoInit().add(edge);
			}
		}
	}

}
