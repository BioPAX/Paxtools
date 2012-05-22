package org.biopax.paxtools.causality.wrapper;

import org.biopax.paxtools.causality.model.AlterationPack;
import org.biopax.paxtools.causality.model.Node;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.TemplateReaction;
import org.biopax.paxtools.query.model.*;
import org.biopax.paxtools.query.wrapperL3.GraphL3;

/**
 * @author Ozgun Babur
 */
public class TemplateReactionWrapper extends
	org.biopax.paxtools.query.wrapperL3.TemplateReactionWrapper implements Node
{
	protected TemplateReactionWrapper(TemplateReaction tempReac, GraphL3 graph)
	{
		super(tempReac, graph);
	}

	@Override
	public AlterationPack getAlterations()
	{
		return null;
	}

	protected void addToUpstream(BioPAXElement ele, org.biopax.paxtools.query.model.Graph graph)
	{
		AbstractNode node = (AbstractNode) graph.getGraphObject(ele);
		Edge edge = new Edge(node, this, (Graph) graph);

		if (node instanceof ControlWrapper)
		{
			if (isTranscription())
			{
				((ControlWrapper) node).setTranscription(true);
			}
			
			edge.setSign(node.getSign());
		}

		node.getDownstreamNoInit().add(edge);
		this.getUpstreamNoInit().add(edge);
	}

}
