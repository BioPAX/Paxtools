package org.biopax.paxtools.model.level3;

public interface SmallMoleculeReference extends EntityReference
{
// -------------------------- OTHER METHODS --------------------------

    // Property CHEMICAL-FORMULA

    String getChemicalFormula();

    void setChemicalFormula(String newCHEMICAL_FORMULA);


    // Property MOLECULAR-WEIGHT

    float getMolecularWeight();

     void setMolecularWeight(float newMOLECULAR_WEIGHT);


    // Property STRUCTURE

    ChemicalStructure getStructure();

     void setStructure(ChemicalStructure newSTRUCTURE);
}
