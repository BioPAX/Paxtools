/*
 * SmallMoleculeProxy.java
 *
 * 2007.04.05 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.chemicalStructure;
import org.biopax.paxtools.model.level2.smallMolecule;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.util.Set;

/**
 * Proxy for smallMolecule
 */
@Entity(name="l2smallmolecule")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class smallMoleculeProxy extends physicalEntityProxy implements smallMolecule {
	public smallMoleculeProxy() {
	}

	@Transient
	public Class getModelInterface()
	{
		return smallMolecule.class;
	}

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity=physicalEntityParticipantProxy.class)
	@JoinTable(name="l2smallmolecule_structure")
	public Set<chemicalStructure> getSTRUCTURE() {
		return ((smallMolecule)object).getSTRUCTURE();
	}

	public void setSTRUCTURE(Set<chemicalStructure> STRUCTURE) {
		((smallMolecule)object).setSTRUCTURE(STRUCTURE);
	}

	public void addSTRUCTURE(chemicalStructure STRUCTURE) {
		((smallMolecule)object).addSTRUCTURE(STRUCTURE);
	}

	public void removeSTRUCTURE(chemicalStructure STRUCTURE) {
		((smallMolecule)object).removeSTRUCTURE(STRUCTURE);
	}

	@Basic @Column(name="chemical_formula_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
	public String getCHEMICAL_FORMULA() {
		return ((smallMolecule)object).getCHEMICAL_FORMULA();
	}

	public void setCHEMICAL_FORMULA(String CHEMICAL_FORMULA) {
		((smallMolecule)object).setCHEMICAL_FORMULA(CHEMICAL_FORMULA);
	}

	@Transient
	//@Basic @Column(columnDefinition="text")
	public double getMOLECULAR_WEIGHT() {
		return stringToDouble(getMOLECULAR_WEIGHT_x());
		//return ((smallMolecule)object).getMOLECULAR_WEIGHT();
	}

	public void setMOLECULAR_WEIGHT(double MOLECULAR_WEIGHT) {
		setMOLECULAR_WEIGHT_x(doubleToString(MOLECULAR_WEIGHT));
		//((smallMolecule)object).setMOLECULAR_WEIGHT(MOLECULAR_WEIGHT);
	}

	@Basic @Column(name="molecular_weight_x", columnDefinition="text")
	protected String getMOLECULAR_WEIGHT_x() {
		return doubleToString(((smallMolecule)object).getMOLECULAR_WEIGHT());
	}

	protected void setMOLECULAR_WEIGHT_x(String s) {
		((smallMolecule)object).setMOLECULAR_WEIGHT(stringToDouble(s));
	}

}
