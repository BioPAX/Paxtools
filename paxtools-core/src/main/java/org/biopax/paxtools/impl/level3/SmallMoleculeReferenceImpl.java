package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.ChemicalStructure;
import org.biopax.paxtools.model.level3.SmallMoleculeReference;


public class SmallMoleculeReferenceImpl extends EntityReferenceImpl implements SmallMoleculeReference
{
	private String chemicalFormula;
	private float molecularWeight = UNKNOWN_FLOAT;
	private ChemicalStructure structure;

	
	public SmallMoleculeReferenceImpl() {
	}

	//
	// utilityClass interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

    @Override
	public Class<? extends SmallMoleculeReference> getModelInterface()
	{
		return SmallMoleculeReference.class;
	}

    public String getChemicalFormula()
	{
		return chemicalFormula;
	}

    public void setChemicalFormula(String formula)
	{
		chemicalFormula = formula;
	}

    // Property MOLECULAR-WEIGHT
    
    public float getMolecularWeight()
	{
		return molecularWeight;
	}

    public void setMolecularWeight(float molecularWeight)
	{
		this.molecularWeight = molecularWeight;
	}

    // Property structure
    public ChemicalStructure getStructure()
	{
		return structure;
	}

    public void setStructure(ChemicalStructure structure)
	{
		this.structure = structure;
	}
    
    @Override
    protected boolean semanticallyEquivalent(BioPAXElement element) {
    	if(!(element instanceof SmallMoleculeReference)) return false;
    	SmallMoleculeReference that = (SmallMoleculeReference) element;
    	return (getChemicalFormula() != null 
    			? getChemicalFormula().equalsIgnoreCase(that.getChemicalFormula()) 
    			: that.getChemicalFormula() == null)
    		&& (getMolecularWeight() == that.getMolecularWeight())
    		&& (getStructure() != null 
        			? getStructure().isEquivalent(that.getStructure()) 
        	    	: that.getStructure() == null)
    		&& super.semanticallyEquivalent(element);
    }
}
