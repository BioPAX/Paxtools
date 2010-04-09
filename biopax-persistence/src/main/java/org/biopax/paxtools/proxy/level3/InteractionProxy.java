/*
 * InteractionProxy.java
 *
 * 2007.11.30 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Entity;

import javax.persistence.*;

import java.util.Set;

/**
 * Proxy for interaction
 */
@javax.persistence.Entity(name="l3interaction")
public class InteractionProxy<T extends Interaction> extends ProcessProxy<T> implements Interaction {

	// Property INTERACTION-TYPE

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = ControlledVocabularyProxy.class, fetch=FetchType.EAGER)
	@JoinTable(name="l3interaction_interact_type_x")
	public Set<ControlledVocabulary> getInteractionType() {
		return object.getInteractionType();
	}

	public void addInteractionType(ControlledVocabulary newINTERACTION_TYPE) {
		object.addInteractionType(newINTERACTION_TYPE);
	}

	public void removeInteractionType(ControlledVocabulary oldINTERACTION_TYPE) {
		object.removeInteractionType(oldINTERACTION_TYPE);
	}

	public void setInteractionType(Set<ControlledVocabulary> newINTERACTION_TYPE) {
		object.setInteractionType(newINTERACTION_TYPE);
	}

	// Property PARTICIPANT

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity= EntityProxy.class, fetch=FetchType.EAGER)
	@JoinTable(name="l3interaction_participants_x")
	public Set<Entity> getParticipant() {
		return object.getParticipant();
	}

	public void addParticipant(Entity aParticipant) {
		object.addParticipant(aParticipant);
	}

	public void removeParticipant(Entity aParticipant) {
		object.removeParticipant(aParticipant);
	}

	public void setParticipant(Set<Entity> PARTICIPANTS) {
		object.setParticipant(PARTICIPANTS);
	}

	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return Interaction.class;
	}
}
