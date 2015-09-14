package org.biopax.paxtools.model.level2;

import java.util.Set;


public interface smallMolecule extends physicalEntity
{
// -------------------------- OTHER METHODS --------------------------

	void addSTRUCTURE(chemicalStructure STRUCTURE);

	String getCHEMICAL_FORMULA();


	double getMOLECULAR_WEIGHT();


	Set<chemicalStructure> getSTRUCTURE();

	void removeSTRUCTURE(chemicalStructure STRUCTURE);

	void setCHEMICAL_FORMULA(String CHEMICAL_FORMULA);

	void setMOLECULAR_WEIGHT(double MOLECULAR_WEIGHT);

	void setSTRUCTURE(Set<chemicalStructure> STRUCTURE);
}