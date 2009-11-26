/*
 * PhysicalEntitySearch.java
 *
 * 2008.01.08 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.persistence.level3;

import java.util.Set;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.proxy.level3.*;

/**
 * PhysicalEntity search class
 * @author yoneki
 */
public class PhysicalEntitySearch extends BaseSearch {
	/**
	 * construct
	 * @param session
	 */
	public PhysicalEntitySearch(HiRDBSession session) {
		super(session);
	}

	/**
	 * get all PhysicalEntity
	 * @return all PhysicalEntity
	 */
	public Set<PhysicalEntity> getPhysicalEntityList() {
		return getAllOneClassList(PhysicalEntity.class, PhysicalEntityProxy.class);
	}

	/**
	 * get all Complex
	 * @return all Complex
	 */
	public Set<Complex> getComplexList() {
		return getAllOneClassList(Complex.class, ComplexProxy.class);
	}

	/**
	 * get all Dna
	 * @return all Dna
	 */
	public Set<Dna> getDnaList() {
		return getAllOneClassList(Dna.class, DnaProxy.class);
	}

	/**
	 * get all Protein
	 * @return all Protein
	 */
	public Set<Protein> getProteinList() {
		return getAllOneClassList(Protein.class, ProteinProxy.class);
	}

	/**
	 * get all Rna
	 * @return all Rna
	 */
	public Set<Rna> getRnaList() {
		return getAllOneClassList(Rna.class, RnaProxy.class);
	}

	/**
	 * get all SmallMolecule
	 * @return all SmallMolecule
	 */
	public Set<SmallMolecule> getSmallMoleculeList() {
		return getAllOneClassList(SmallMolecule.class, SmallMoleculeProxy.class);
	}

	/**
	 * get PhysicalEntity by Name (level 3 is different)
	 * @return PhysicalEntity
	 */
	public Set<PhysicalEntity> getPhysicalEntityListByName(String regex) {
		return getOneClassByName(PhysicalEntity.class, regex);
	}
}
