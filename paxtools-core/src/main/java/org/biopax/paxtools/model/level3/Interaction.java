package org.biopax.paxtools.model.level3;

import java.util.Set;


public interface Interaction extends Process
{
	// Property INTERACTION-TYPE

	Set<InteractionVocabulary> getInteractionType();

	void addInteractionType(InteractionVocabulary newINTERACTION_TYPE);

	void removeInteractionType(InteractionVocabulary oldINTERACTION_TYPE);


	// Property PARTICIPANT

	Set<Entity> getParticipant();

	void addParticipant(Entity participant);

	void removeParticipant(Entity participant);



}
