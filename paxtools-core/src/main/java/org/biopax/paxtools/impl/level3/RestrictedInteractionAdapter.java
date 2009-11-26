package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.Interaction;

import java.util.Set;

/**
 */
abstract class RestrictedInteractionAdapter extends InteractionImpl
        implements Interaction {

    @Override
    public void addParticipant(Entity participants) {
        throw new UnsupportedOperationException(
                "Directly setting participant is not allowed!");
    }

    @Override
    public void removeParticipant(Entity participants) {
        throw new UnsupportedOperationException(
                "Directly setting participant is not allowed!");
    }

    @Override
    public void setParticipant(Set<Entity> participants) {
        throw new UnsupportedOperationException(
                "Directly setting participant is not allowed!");
    }

    void addSubParticipant(Entity participant) {
        super.addParticipant(participant);
    }

    void removeSubParticipant(Entity participant) {
        super.removeParticipant(participant);
    }

}
