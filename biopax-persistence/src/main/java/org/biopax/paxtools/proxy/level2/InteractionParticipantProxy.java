/*
 * InteractionParticipantProxy.java
 *
 * 2007.04.05 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.InteractionParticipant;
import org.biopax.paxtools.model.level2.interaction;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Set;

/**
 * Proxy for InteractionParticipant
 */
@Entity(name="l2interactionparticipant")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public abstract class InteractionParticipantProxy extends Level2ElementProxy implements InteractionParticipant, Serializable {
	protected InteractionParticipantProxy() {
	}

	@Transient
	public Set<interaction> isPARTICIPANTSof() {
		return ((InteractionParticipant)object).isPARTICIPANTSof();
	}
}

