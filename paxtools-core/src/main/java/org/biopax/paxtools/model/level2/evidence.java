package org.biopax.paxtools.model.level2;

import java.util.Set;


public interface evidence extends utilityClass, XReferrable
{
// -------------------------- OTHER METHODS --------------------------

	public void addCONFIDENCE(confidence CONFIDENCE);

	public void addEVIDENCE_CODE(openControlledVocabulary EVIDENCE_CODE);

	public void addEXPERIMENTAL_FORM(experimentalForm EXPERIMENTAL_FORM);


	public Set<confidence> getCONFIDENCE();


	public Set<openControlledVocabulary> getEVIDENCE_CODE();
// --------------------- ACCESORS and MUTATORS---------------------

	public Set<experimentalForm> getEXPERIMENTAL_FORM();

	public void removeCONFIDENCE(confidence CONFIDENCE);

	public void removeEVIDENCE_CODE(openControlledVocabulary EVIDENCE_CODE);

	public void removeEXPERIMENTAL_FORM(experimentalForm EXPERIMENTAL_FORM);

	void setCONFIDENCE(Set<confidence> CONFIDENCE);

	void setEVIDENCE_CODE(Set<openControlledVocabulary> EVIDENCE_CODE);

	void setEXPERIMENTAL_FORM(Set<experimentalForm> EXPERIMENTAL_FORM);
}