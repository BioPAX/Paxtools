package org.biopax.paxtools.model.level2;

import java.util.Set;


public interface conversion extends physicalInteraction
{
	Set<physicalEntityParticipant> getLEFT();

	Set<physicalEntityParticipant> getRIGHT();

	SpontaneousType getSPONTANEOUS();

	void addLEFT(physicalEntityParticipant LEFT);

	void addRIGHT(physicalEntityParticipant RIGHT);

	void removeLEFT(physicalEntityParticipant LEFT);

	void removeRIGHT(physicalEntityParticipant RIGHT);

	void setLEFT(Set<physicalEntityParticipant> LEFT);

	void setRIGHT(Set<physicalEntityParticipant> RIGHT);

	void setSPONTANEOUS(SpontaneousType SPONTANEOUS);
}