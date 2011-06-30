package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.ChemicalStructure;
import org.biopax.paxtools.model.level3.SmallMoleculeReference;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
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

    @Override  @Transient
	public Class<? extends SmallMoleculeReference> getModelInterface()
	{
		return SmallMoleculeReference.class;
	}

    
    
    @Field(name=BioPAXElementImpl.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
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
    @ManyToOne(targetEntity = ChemicalStructureImpl.class)//, cascade={CascadeType.ALL})
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
