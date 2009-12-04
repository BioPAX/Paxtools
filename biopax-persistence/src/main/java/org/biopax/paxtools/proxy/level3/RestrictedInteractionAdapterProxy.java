package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.Interaction;

import java.util.Set;

import javax.persistence.Transient;

abstract class RestrictedInteractionAdapterProxy extends InteractionProxy
        implements Interaction {

	@Transient
    @Override
    public void addParticipant(Entity participants) {
        throw new UnsupportedOperationException(
                "Directly setting participant is not allowed!");
    }

	@Transient
    @Override
    public void removeParticipant(Entity participants) {
        throw new UnsupportedOperationException(
                "Directly setting participant is not allowed!");
    }

    @Transient
    @Override
    public void setParticipant(Set<Entity> participants) {
        throw new UnsupportedOperationException(
                "Directly setting participant is not allowed!");
    }

    @Transient
    void addSubParticipant(Entity participant) {
        super.addParticipant(participant);
    }

    @Transient
    void removeSubParticipant(Entity participant) {
        super.removeParticipant(participant);
    }

}
