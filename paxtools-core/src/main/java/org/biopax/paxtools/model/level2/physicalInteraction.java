package org.biopax.paxtools.model.level2;

import java.util.Set;


public interface physicalInteraction extends interaction
{

	void addINTERACTION_TYPE(openControlledVocabulary INTERACTION_TYPE);

	Set<openControlledVocabulary> getINTERACTION_TYPE();

	void removeINTERACTION_TYPE(
		openControlledVocabulary INTERACTION_TYPE);

	void setINTERACTION_TYPE(Set<openControlledVocabulary> INTERACTION_TYPE);
}