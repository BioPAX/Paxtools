package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.MolecularInteraction;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

/**
 */
class MolecularInteractionImpl extends InteractionImpl
        implements MolecularInteraction
{
// --------------------------- CONSTRUCTORS ---------------------------

    public MolecularInteractionImpl()
    {
    }


    public Class<? extends MolecularInteraction> getModelInterface()
    {
        return MolecularInteraction.class;
    }

    public void addParticipant(Entity participant)
    {
        if (participant instanceof PhysicalEntity)
        {
            super.addParticipant(participant);
        }
        else
        {
            throw new IllegalBioPAXArgumentException
                    ("Participants of a molecular interaction should be" +
                     "of type PhysicalEntity. Parameter " +
                     participant + "is of type " + participant.getClass());
        }

    }
}
