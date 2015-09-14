package org.biopax.paxtools.model.level2;

import java.util.Set;

public interface control extends physicalInteraction
{
// -------------------------- OTHER METHODS --------------------------

	void addCONTROLLED(process CONTROLLED);

	void addCONTROLLER(physicalEntityParticipant CONTROLLER);

	Set<process> getCONTROLLED();

	Set<physicalEntityParticipant> getCONTROLLER();
// --------------------- ACCESORS and MUTATORS---------------------

	ControlType getCONTROL_TYPE();

	void removeCONTROLLED(process CONTROLLED);

	void removeCONTROLLER(physicalEntityParticipant CONTROLLER);

	void setCONTROLLED(Set<process> CONTROLLED);

	void setCONTROLLER(Set<physicalEntityParticipant> CONTROLLER);

	void setCONTROL_TYPE(ControlType CONTROL_TYPE);
}