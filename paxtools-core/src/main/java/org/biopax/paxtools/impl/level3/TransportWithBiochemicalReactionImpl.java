package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.TransportWithBiochemicalReaction;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@Indexed
class TransportWithBiochemicalReactionImpl extends BiochemicalReactionImpl
	implements TransportWithBiochemicalReaction
{
    @Transient
    public Class<? extends TransportWithBiochemicalReaction> getModelInterface()
        {
            return TransportWithBiochemicalReaction.class;
        }

}
