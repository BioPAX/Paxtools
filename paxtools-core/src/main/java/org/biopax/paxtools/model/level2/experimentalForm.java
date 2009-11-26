package org.biopax.paxtools.model.level2;

import java.util.Set;


public interface experimentalForm extends utilityClass
{
// -------------------------- OTHER METHODS --------------------------

	public void addEXPERIMENTAL_FORM_TYPE(
		openControlledVocabulary EXPERIMENTAL_FORM_TYPE);

	public Set<openControlledVocabulary> getEXPERIMENTAL_FORM_TYPE();
// --------------------- ACCESORS and MUTATORS---------------------

	public physicalEntityParticipant getPARTICIPANT();

	public void removeEXPERIMENTAL_FORM_TYPE(
		openControlledVocabulary EXPERIMENTAL_FORM_TYPE);

	public void setEXPERIMENTAL_FORM_TYPE(
		Set<openControlledVocabulary> EXPERIMENTAL_FORM_TYPE);

	public void setPARTICIPANT(physicalEntityParticipant PARTICIPANT);
}