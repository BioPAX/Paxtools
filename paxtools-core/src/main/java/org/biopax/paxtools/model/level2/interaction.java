package org.biopax.paxtools.model.level2;

import java.util.Set;


public interface interaction extends process
{
	// --------------------- ACCESORS and MUTATORS---------------------

	void addPARTICIPANTS(InteractionParticipant aParticipant);

	Set<InteractionParticipant> getPARTICIPANTS();

	void removePARTICIPANTS(InteractionParticipant aParticipant);

	void setPARTICIPANTS(Set<InteractionParticipant> PARTICIPANTS);
	
}