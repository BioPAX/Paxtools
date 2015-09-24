package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.TransportWithBiochemicalReaction;


public class TransportWithBiochemicalReactionImpl extends BiochemicalReactionImpl
	implements TransportWithBiochemicalReaction
{
	public TransportWithBiochemicalReactionImpl() {}
	
    public Class<? extends TransportWithBiochemicalReaction> getModelInterface()
    {
        return TransportWithBiochemicalReaction.class;
    }

}
