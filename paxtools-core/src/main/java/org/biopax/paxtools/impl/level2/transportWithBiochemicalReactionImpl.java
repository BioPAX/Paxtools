package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.transportWithBiochemicalReaction;

class transportWithBiochemicalReactionImpl
	extends biochemicalReactionImpl
	implements transportWithBiochemicalReaction
{
	@Override public Class<? extends BioPAXElement> getModelInterface()
	{
		return transportWithBiochemicalReaction.class;
	}
}
