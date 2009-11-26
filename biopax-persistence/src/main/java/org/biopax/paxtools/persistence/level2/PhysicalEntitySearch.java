/*
 * PhysicalEntitySearch.java
 *
 * 2007.08.02 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.persistence.level2;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.bioSource;
import org.biopax.paxtools.model.level2.complex;
import org.biopax.paxtools.model.level2.dna;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level2.protein;
import org.biopax.paxtools.model.level2.rna;
import org.biopax.paxtools.model.level2.sequenceEntity;
import org.biopax.paxtools.model.level2.smallMolecule;
import org.biopax.paxtools.proxy.level2.BioPAXElementProxy;
import org.biopax.paxtools.proxy.level2.complexProxy;
import org.biopax.paxtools.proxy.level2.dnaProxy;
import org.biopax.paxtools.proxy.level2.physicalEntityProxy;
import org.biopax.paxtools.proxy.level2.proteinProxy;
import org.biopax.paxtools.proxy.level2.rnaProxy;
import org.biopax.paxtools.proxy.level2.sequenceEntityProxy;
import org.biopax.paxtools.proxy.level2.smallMoleculeProxy;

/**
 * physicalEntity search class
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
	 * get all physical entity
	 * @return all physical entity
	 */
	public Set<physicalEntity> getPhysicalEntityList() {
		return getAllOneClassList(physicalEntity.class, physicalEntityProxy.class);
	}

	/**
	 * get all complex
	 * @return all complex
	 */
	public Set<complex> getComplexList() {
		return getAllOneClassList(complex.class, complexProxy.class);
	}

	/**
	 * get all dna
	 * @return all dna
	 */
	public Set<dna> getDnaList() {
		return getAllOneClassList(dna.class, dnaProxy.class);
	}

	/**
	 * get all protein
	 * @return all protein
	 */
	public Set<protein> getProteinList() {
		return getAllOneClassList(protein.class, proteinProxy.class);
	}

	/**
	 * get all rna
	 * @return all rna
	 */
	public Set<rna> getRnaList() {
		return getAllOneClassList(rna.class, rnaProxy.class);
	}

	/**
	 * get all smallMolecule
	 * @return all smallMolecule
	 */
	public Set<smallMolecule> getSmallMoleculeList() {
		return getAllOneClassList(smallMolecule.class, smallMoleculeProxy.class);
	}

	/**
	 * get physicalEntity by NAME
	 * @return physicalEntity
	 */
	public Set<physicalEntity> getPhysicalEntityListByNAME(String regex, boolean bIncludeSynonyms) {
		return getOneClassByNAME(physicalEntity.class, regex, bIncludeSynonyms);
	}

	/**
	 * get physicalEntity by ORGANISM
	 * @return physicalEntity
	 */
	public Set<physicalEntity> getPhysicalEntityListByORGANISM(String regex) {
		HashSet result = new HashSet();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		KeywordSearch ks = session.createKeywordSearch();
		List<BioPAXElement> bss = ks.fetch(em, BioPAXElementProxy.SEARCH_FIELD_NAME, regex, bioSource.class);
		for (BioPAXElement bs: bss) {
			List cs = em.createQuery(
				"select o from " + complexProxy.class.getName() + " as o where o.ORGANISM.RDFId = '" + 
					bs.getRDFId() + "'").getResultList();
			if (cs != null)
				result.addAll(cs);
			List ses = em.createQuery(
				"select o from " + sequenceEntityProxy.class.getName() + " as o where o.ORGANISM.RDFId = '" + 
					bs.getRDFId() + "'").getResultList();
			if (ses != null)
				result.addAll(ses);
		}
		return result;
	}

	/**
	 * get sequenceEntity by SEQUENCE
	 * @return sequenceEntity
	 */
	public Set<sequenceEntity> getSequenceEntityListBySEQUENCE(String sequence, boolean bExactMatch) {
		Set result = new HashSet();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		KeywordSearch ks = session.createKeywordSearch();
		// Luceneでは部分文字列検索がきちんと表現できない。
		// 2007.0803 Takeshi Yoneki
		String keyword = bExactMatch ? sequence : sequence + "*";
		result.addAll(ks.fetch(em, BioPAXElementProxy.SEARCH_FIELD_SEQUENCE, keyword, sequenceEntity.class));
		return result;
	}
}
