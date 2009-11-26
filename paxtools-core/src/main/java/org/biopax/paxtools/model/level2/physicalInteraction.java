package org.biopax.paxtools.model.level2;

import java.util.Set;


public interface physicalInteraction extends interaction
{

	public void addINTERACTION_TYPE(openControlledVocabulary INTERACTION_TYPE);

	public Set<openControlledVocabulary> getINTERACTION_TYPE();

	public void removeINTERACTION_TYPE(
		openControlledVocabulary INTERACTION_TYPE);

	void setINTERACTION_TYPE(Set<openControlledVocabulary> INTERACTION_TYPE);
}