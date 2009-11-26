package org.biopax.paxtools.model.level2;

import java.util.Set;


public interface openControlledVocabulary
	extends externalReferenceUtilityClass, XReferrable
{
// -------------------------- OTHER METHODS --------------------------

	public void addTERM(String TERM);
// --------------------- ACCESORS and MUTATORS---------------------

	public Set<String> getTERM();

	public void removeTERM(String TERM);

	void setTERM(Set<String> TERM);
}