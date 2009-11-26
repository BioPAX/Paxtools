package org.biopax.paxtools.model.level2;

import java.util.Set;


public interface sequenceFeature extends utilityClass, XReferrable
{
// -------------------------- OTHER METHODS --------------------------

	public void addFEATURE_LOCATION(sequenceLocation FEATURE_LOCATION);

	public void addSYNONYMS(String SYNONYMS);


	public Set<sequenceLocation> getFEATURE_LOCATION();
// --------------------- ACCESORS and MUTATORS---------------------

	public openControlledVocabulary getFEATURE_TYPE();


	public String getNAME();


	public String getSHORT_NAME();


	public Set<String> getSYNONYMS();

	public void removeFEATURE_LOCATION(sequenceLocation FEATURE_LOCATION);

	public void removeSYNONYMS(String SYNONYMS);

	void setFEATURE_LOCATION(Set<sequenceLocation> FEATURE_LOCATION);

	public void setFEATURE_TYPE(openControlledVocabulary FEATURE_TYPE);

	public void setNAME(String NAME);

	public void setSHORT_NAME(String SHORT_NAME);

	void setSYNONYMS(Set<String> SYNONYMS);
}