package org.biopax.paxtools.model.level2;

import java.util.Set;


public interface interaction extends process
{
	// --------------------- ACCESORS and MUTATORS---------------------

	public void addPARTICIPANTS(InteractionParticipant aParticipant);

	public Set<InteractionParticipant> getPARTICIPANTS();

	public void removePARTICIPANTS(InteractionParticipant aParticipant);

	public void setPARTICIPANTS(Set<InteractionParticipant> PARTICIPANTS);
	
}