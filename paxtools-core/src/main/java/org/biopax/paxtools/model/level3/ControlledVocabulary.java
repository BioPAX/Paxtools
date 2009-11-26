package org.biopax.paxtools.model.level3;

import java.util.Set;


public interface ControlledVocabulary
	extends UtilityClass, XReferrable
{

    // Property TERM

    Set<String> getTerm();

    void addTerm(String newTERM);

    void removeTerm(String oldTERM);

    void setTerm(Set<String> newTERM);

	
}
