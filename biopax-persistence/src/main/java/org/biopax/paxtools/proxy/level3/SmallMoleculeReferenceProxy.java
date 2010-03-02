/*
 * SmallMoleculeReferenceProxy.java
 *
 * 2007.12.04 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.*;

import javax.persistence.Entity;
import javax.persistence.*;

/**
 * Proxy for SmallMoleculeReference
 */
@Entity(name="l3smallmoleculereference")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class SmallMoleculeReferenceProxy extends EntityReferenceProxy 
	implements SmallMoleculeReference 
{
	public SmallMoleculeReferenceProxy() {
	}

    // Property CHEMICAL-FORMULA

	@Basic @Column(name="chemical_formula_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
	public String getChemicalFormula() {
		return ((SmallMoleculeReference)object).getChemicalFormula();
	}

	public void setChemicalFormula(String CHEMICAL_FORMULA) {
		((SmallMoleculeReference)object).setChemicalFormula(CHEMICAL_FORMULA);
	}

    // Property MOLECULAR-WEIGHT

	@Basic @Column(name="molecular_weight_x", columnDefinition="text")
	protected String getMolecularWeight_x() {
		return floatToString(((SmallMoleculeReference)object).getMolecularWeight());
	}

	protected void setMolecularWeight_x(String s) {
		((SmallMoleculeReference)object).setMolecularWeight(stringToFloat(s));
	}

	@Transient
	public float getMolecularWeight() {
		return stringToFloat(getMolecularWeight_x());
	}

	public void setMolecularWeight(float MOLECULAR_WEIGHT) {
		setMolecularWeight_x(floatToString(MOLECULAR_WEIGHT));
	}

    // Property STRUCTURE

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity= ChemicalStructureProxy.class)
	@JoinColumn(name="structure_x")
	public ChemicalStructure getStructure() {
		return ((SmallMoleculeReference)object).getStructure();
	}

	public void setStructure(ChemicalStructure newSTRUCTURE) {
		((SmallMoleculeReference)object).setStructure(newSTRUCTURE);
	}
	
	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return SmallMoleculeReference.class;
	}
}
