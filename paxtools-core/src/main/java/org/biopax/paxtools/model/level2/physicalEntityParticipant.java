package org.biopax.paxtools.model.level2;


public interface physicalEntityParticipant
	extends utilityClass, InteractionParticipant

{
// -------------------------- OTHER METHODS --------------------------

// --------------------- ACCESORS and MUTATORS---------------------

	public physicalEntity getPHYSICAL_ENTITY();
	public void setPHYSICAL_ENTITY(physicalEntity PHYSICAL_ENTITY);

	public double getSTOICHIOMETRIC_COEFFICIENT();
	public void setSTOICHIOMETRIC_COEFFICIENT(
		double STOICHIOMETRIC_COEFFICIENT);

	public complex isCOMPONENTof();
    public void setCOMPONENTSof(complex aComplex);

	public openControlledVocabulary getCELLULAR_LOCATION();

	public void setCELLULAR_LOCATION(
		openControlledVocabulary CELLULAR_LOCATION);


	boolean isInEquivalentState(physicalEntityParticipant that);
	public int stateCode();
}
