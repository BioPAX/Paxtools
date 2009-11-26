package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.TransportWithBiochemicalReaction;
import org.biopax.paxtools.model.level3.BiochemicalReaction;
import org.biopax.paxtools.model.BioPAXElement;

/**
 */
class TransportWithBiochemicalReactionImpl
	extends BiochemicalReactionImpl
	implements TransportWithBiochemicalReaction
{
    public Class<? extends TransportWithBiochemicalReaction> getModelInterface()
        {
            return TransportWithBiochemicalReaction.class;
        }

}
