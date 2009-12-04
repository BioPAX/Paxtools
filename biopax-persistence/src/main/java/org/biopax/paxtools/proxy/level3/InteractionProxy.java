/*
 * InteractionProxy.java
 *
 * 2007.11.30 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Entity;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import java.util.Set;

/**
 * Proxy for interaction
 */
@javax.persistence.Entity(name="l3interaction")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class InteractionProxy extends ProcessProxy implements Interaction {
	public InteractionProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return Interaction.class;
	}

	Set<Entity> proxyPARTICIPANT = null;

	// Property INTERACTION-TYPE

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = ControlledVocabularyProxy.class, fetch=FetchType.EAGER)
	@JoinTable(name="l3interaction_interact_type_x")
	public Set<ControlledVocabulary> getInteractionType() {
		return ((Interaction)object).getInteractionType();
	}

	public void addInteractionType(ControlledVocabulary newINTERACTION_TYPE) {
		((Interaction)object).addInteractionType(newINTERACTION_TYPE);
	}

	public void removeInteractionType(ControlledVocabulary oldINTERACTION_TYPE) {
		((Interaction)object).removeInteractionType(oldINTERACTION_TYPE);
	}

	public void setInteractionType(Set<ControlledVocabulary> newINTERACTION_TYPE) {
		((Interaction)object).setInteractionType(newINTERACTION_TYPE);
	}

	// Property PARTICIPANT

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity= EntityProxy.class, fetch=FetchType.EAGER)
	@JoinTable(name="l3interaction_participants_x")
	protected Set<Entity> getParticipant_x() {
		if (proxyPARTICIPANT == null)
			proxyPARTICIPANT = getParticipant();
		return proxyPARTICIPANT;
	}

	protected void setParticipant_x(Set<Entity> PARTICIPANTS) {
		call_setParticipant_x(PARTICIPANTS);
	}

	@Transient
	public Set<Entity> getParticipant() {
		return ((Interaction)object).getParticipant();
	}

	public void addParticipant(Entity aParticipant) {
		((Interaction)object).addParticipant(aParticipant);
		proxyPARTICIPANT = null;
	}

	public void removeParticipant(Entity aParticipant) {
		((Interaction)object).removeParticipant(aParticipant);
		proxyPARTICIPANT = null;
	}

	public void setParticipant(Set<Entity> PARTICIPANTS) {
		((Interaction)object).setParticipant(PARTICIPANTS);
	}

	protected void call_setParticipant_x(Set<Entity> PARTICIPANTS) {
		try {
			Set<Entity> olsIPs = getParticipant();
			for (Entity oip: olsIPs)
				removeParticipant(oip);
			for (Entity ip: PARTICIPANTS)
				addParticipant(ip);
		}
		catch (Exception e) {
			// Directly setting participants are not allowed !
			System.err.println(e.getMessage());
		}
		proxyPARTICIPANT = PARTICIPANTS;
	}

}
