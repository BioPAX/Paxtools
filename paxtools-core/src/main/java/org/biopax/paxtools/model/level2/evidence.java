package org.biopax.paxtools.model.level2;

import java.util.Set;


public interface evidence extends utilityClass, XReferrable
{
// -------------------------- OTHER METHODS --------------------------

	void addCONFIDENCE(confidence CONFIDENCE);

	void addEVIDENCE_CODE(openControlledVocabulary EVIDENCE_CODE);

	void addEXPERIMENTAL_FORM(experimentalForm EXPERIMENTAL_FORM);


	Set<confidence> getCONFIDENCE();


	Set<openControlledVocabulary> getEVIDENCE_CODE();
// --------------------- ACCESORS and MUTATORS---------------------

	Set<experimentalForm> getEXPERIMENTAL_FORM();

	void removeCONFIDENCE(confidence CONFIDENCE);

	void removeEVIDENCE_CODE(openControlledVocabulary EVIDENCE_CODE);

	void removeEXPERIMENTAL_FORM(experimentalForm EXPERIMENTAL_FORM);

	void setCONFIDENCE(Set<confidence> CONFIDENCE);

	void setEVIDENCE_CODE(Set<openControlledVocabulary> EVIDENCE_CODE);

	void setEXPERIMENTAL_FORM(Set<experimentalForm> EXPERIMENTAL_FORM);
}