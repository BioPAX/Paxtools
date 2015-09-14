package org.biopax.paxtools.model.level2;

import java.util.Set;


public interface openControlledVocabulary
	extends externalReferenceUtilityClass, XReferrable
{
// -------------------------- OTHER METHODS --------------------------

	void addTERM(String TERM);
// --------------------- ACCESORS and MUTATORS---------------------

	Set<String> getTERM();

	void removeTERM(String TERM);

	void setTERM(Set<String> TERM);
}