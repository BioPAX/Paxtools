package org.biopax.paxtools.model.level2;

import java.util.Set;


public interface smallMolecule extends physicalEntity
{
// -------------------------- OTHER METHODS --------------------------

	public void addSTRUCTURE(chemicalStructure STRUCTURE);

	public String getCHEMICAL_FORMULA();


	public double getMOLECULAR_WEIGHT();


	public Set<chemicalStructure> getSTRUCTURE();

	public void removeSTRUCTURE(chemicalStructure STRUCTURE);

	public void setCHEMICAL_FORMULA(String CHEMICAL_FORMULA);

	public void setMOLECULAR_WEIGHT(double MOLECULAR_WEIGHT);

	void setSTRUCTURE(Set<chemicalStructure> STRUCTURE);
}