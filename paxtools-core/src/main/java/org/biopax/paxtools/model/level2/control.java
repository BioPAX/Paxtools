package org.biopax.paxtools.model.level2;

import java.util.Set;

public interface control extends physicalInteraction
{
// -------------------------- OTHER METHODS --------------------------

	public void addCONTROLLED(process CONTROLLED);

	public void addCONTROLLER(physicalEntityParticipant CONTROLLER);

	public Set<process> getCONTROLLED();

	public Set<physicalEntityParticipant> getCONTROLLER();
// --------------------- ACCESORS and MUTATORS---------------------

	public ControlType getCONTROL_TYPE();

	public void removeCONTROLLED(process CONTROLLED);

	public void removeCONTROLLER(physicalEntityParticipant CONTROLLER);

	public void setCONTROLLED(Set<process> CONTROLLED);

	public void setCONTROLLER(Set<physicalEntityParticipant> CONTROLLER);

	public void setCONTROL_TYPE(ControlType CONTROL_TYPE);
}