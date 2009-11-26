package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.chemicalStructure;
import org.biopax.paxtools.model.level2.smallMolecule;

import java.util.HashSet;
import java.util.Set;

/**
 */
class smallMoleculeImpl extends physicalEntityImpl
	implements smallMolecule
{
// ------------------------------ FIELDS ------------------------------

	private String CHEMICAL_FORMULA;

	private double MOLECULAR_WEIGHT = BioPAXElement.UNKNOWN_DOUBLE;

	private Set<chemicalStructure> STRUCTURE;

// --------------------------- CONSTRUCTORS ---------------------------

	public smallMoleculeImpl()
	{
		this.STRUCTURE = new HashSet<chemicalStructure>();
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------


	public Class<? extends BioPAXElement> getModelInterface()
	{
		return smallMolecule.class;
	}

// --------------------- Interface smallMolecule ---------------------

// --------------------- ACCESORS and MUTATORS---------------------

	public String getCHEMICAL_FORMULA()
	{
		return CHEMICAL_FORMULA;
	}

	/**
	 * A string representing the Chemical Formula of this small molecule.
	 *
	 * @param CHEMICAL_FORMULA must not be empty. Can be in CML, SMILES or InChI
	 */
	public void setCHEMICAL_FORMULA(String CHEMICAL_FORMULA)
	{
		this.CHEMICAL_FORMULA = CHEMICAL_FORMULA;
	}

	public double getMOLECULAR_WEIGHT()
	{
		return MOLECULAR_WEIGHT;
	}

	public void setMOLECULAR_WEIGHT(double MOLECULAR_WEIGHT)
	{
		this.MOLECULAR_WEIGHT = MOLECULAR_WEIGHT;
	}

	public Set<chemicalStructure> getSTRUCTURE()
	{
		return STRUCTURE;
	}

	/**
	 * Setter for Structure. This method does not validate or maintain internal
	 * links. This method is reserved for performance critical load operation. Use
	 * #addStructure for regular manipuation,
	 */
	public void setSTRUCTURE(Set<chemicalStructure> STRUCTURE)
	{
		this.STRUCTURE = STRUCTURE;
	}

	public void addSTRUCTURE(chemicalStructure STRUCTURE)
	{
		this.STRUCTURE.add(STRUCTURE);
	}

	public void removeSTRUCTURE(chemicalStructure STRUCTURE)
	{
		this.STRUCTURE.remove(STRUCTURE);
	}
}
