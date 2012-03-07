package org.biopax.paxtools.causality.wrapper;

import org.biopax.paxtools.causality.model.Node;
import org.biopax.paxtools.model.level3.TemplateReaction;
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
}
