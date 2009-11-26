package org.biopax.paxtools.model.level2;

import java.util.Set;


public interface physicalEntity extends entity
{
// -------------------------- OTHER METHODS --------------------------

	public Set<physicalEntityParticipant> isPHYSICAL_ENTITYof();

	public Set<interaction> getAllInteractions();
	public <T extends interaction> Set<T> getAllInteractions(
		Class<T> ofType);

	void addPHYSICAL_ENTITYof(physicalEntityParticipant pep);

	void removePHYSICAL_ENTITYof(physicalEntityParticipant pep);
}