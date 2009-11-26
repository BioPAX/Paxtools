package org.biopax.paxtools.model.level2;

import java.util.Set;


public interface conversion extends physicalInteraction
{



	public Set<physicalEntityParticipant> getLEFT();

	public Set<physicalEntityParticipant> getRIGHT();


	public SpontaneousType getSPONTANEOUS();

	public void addLEFT(physicalEntityParticipant LEFT);

	public void addRIGHT(physicalEntityParticipant RIGHT);

	public void removeLEFT(physicalEntityParticipant LEFT);

	public void removeRIGHT(physicalEntityParticipant RIGHT);

	public void setLEFT(Set<physicalEntityParticipant> LEFT);

	public void setRIGHT(Set<physicalEntityParticipant> RIGHT);

	public void setSPONTANEOUS(SpontaneousType SPONTANEOUS);
}