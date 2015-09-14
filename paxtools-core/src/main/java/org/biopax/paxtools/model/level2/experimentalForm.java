package org.biopax.paxtools.model.level2;

import java.util.Set;


public interface experimentalForm extends utilityClass
{
// -------------------------- OTHER METHODS --------------------------

	void addEXPERIMENTAL_FORM_TYPE(
		openControlledVocabulary EXPERIMENTAL_FORM_TYPE);

	Set<openControlledVocabulary> getEXPERIMENTAL_FORM_TYPE();
// --------------------- ACCESORS and MUTATORS---------------------

	physicalEntityParticipant getPARTICIPANT();

	void removeEXPERIMENTAL_FORM_TYPE(
		openControlledVocabulary EXPERIMENTAL_FORM_TYPE);

	void setEXPERIMENTAL_FORM_TYPE(
		Set<openControlledVocabulary> EXPERIMENTAL_FORM_TYPE);

	void setPARTICIPANT(physicalEntityParticipant PARTICIPANT);
}