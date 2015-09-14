package org.biopax.paxtools.model.level2;


public interface physicalEntityParticipant
	extends utilityClass, InteractionParticipant

{
// -------------------------- OTHER METHODS --------------------------

// --------------------- ACCESORS and MUTATORS---------------------

	physicalEntity getPHYSICAL_ENTITY();
	void setPHYSICAL_ENTITY(physicalEntity PHYSICAL_ENTITY);

	double getSTOICHIOMETRIC_COEFFICIENT();
	void setSTOICHIOMETRIC_COEFFICIENT(
		double STOICHIOMETRIC_COEFFICIENT);

	complex isCOMPONENTof();
    void setCOMPONENTSof(complex aComplex);

	openControlledVocabulary getCELLULAR_LOCATION();

	void setCELLULAR_LOCATION(
		openControlledVocabulary CELLULAR_LOCATION);


	boolean isInEquivalentState(physicalEntityParticipant that);
	int stateCode();
}
