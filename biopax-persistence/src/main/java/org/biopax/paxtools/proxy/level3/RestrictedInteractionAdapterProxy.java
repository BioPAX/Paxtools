package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.RestrictedInteraction;

import java.util.Set;

//@javax.persistence.Entity(name="l3restrictedinteraction")
public abstract class RestrictedInteractionAdapterProxy extends InteractionProxy
        implements RestrictedInteraction {

    public void addParticipant(Entity participants) {
        //throw new UnsupportedOperationException("Cannot directly set participant (use left/right properties)!");
    	// adding is done via addLeft, addRight, addController, or addControlled methods...
    }

    public void removeParticipant(Entity participants) {
        //throw new UnsupportedOperationException("Cannot directly set participant (use left/right properties)!");
    }

    public void setParticipant(Set<Entity> participants) {
        //throw new UnsupportedOperationException("Cannot directly set participant (use left/right properties)!");
    	
    }

}
