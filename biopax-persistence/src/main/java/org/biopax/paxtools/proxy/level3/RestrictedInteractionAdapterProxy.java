package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.Interaction;
import org.hibernate.search.annotations.Indexed;

import java.util.Set;
import javax.persistence.*;

@javax.persistence.Entity(name="l3restrictedinteraction")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public abstract class RestrictedInteractionAdapterProxy extends InteractionProxy
        implements Interaction {
	@Transient
    @Override
    public void addParticipant(Entity participants) {
        throw new UnsupportedOperationException(
                "Cannot directly set participant (use left/right properties)!");
    }

	@Transient
    @Override
    public void removeParticipant(Entity participants) {
        throw new UnsupportedOperationException(
                "Cannot directly set participant (use left/right properties)!");
    }

    @Transient
    @Override
    public void setParticipant(Set<Entity> participants) {
        throw new UnsupportedOperationException(
                "Cannot directly set participant (use left/right properties)!");
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
