package org.biopax.paxtools.model.level2;

import java.util.Set;


public interface sequenceFeature extends utilityClass, XReferrable
{
// -------------------------- OTHER METHODS --------------------------

	void addFEATURE_LOCATION(sequenceLocation FEATURE_LOCATION);

	void addSYNONYMS(String SYNONYMS);

	Set<sequenceLocation> getFEATURE_LOCATION();
// --------------------- ACCESORS and MUTATORS---------------------

	openControlledVocabulary getFEATURE_TYPE();

	String getNAME();

	String getSHORT_NAME();

	Set<String> getSYNONYMS();

	void removeFEATURE_LOCATION(sequenceLocation FEATURE_LOCATION);

	void removeSYNONYMS(String SYNONYMS);

	void setFEATURE_LOCATION(Set<sequenceLocation> FEATURE_LOCATION);

	void setFEATURE_TYPE(openControlledVocabulary FEATURE_TYPE);

	void setNAME(String NAME);

	void setSHORT_NAME(String SHORT_NAME);

	void setSYNONYMS(Set<String> SYNONYMS);
}