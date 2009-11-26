/*
 * EntityReferenceSearch.java
 *
 * 2008.01.08 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */
package org.biopax.paxtools.persistence.level3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.proxy.level3.*;

/**
 * PhysicalEntity search class
 * @author yoneki
 */
public class EntityReferenceSearch extends BaseSearch {

	/**
	 * construct
	 * @param session
	 */
	public EntityReferenceSearch(HiRDBSession session) {
		super(session);
	}

	/**
	 * get all EntityReference
	 * @return all EntityReference
	 */
	public Set<EntityReference> getEntityReferenceList() {
		return getAllOneClassList(EntityReference.class, EntityReferenceProxy.class);
	}

	/**
	 * get all DnaReference
	 * @return all DnaReference
	 */
	public Set<DnaReference> getDnaReferenceList() {
		return getAllOneClassList(DnaReference.class, DnaReferenceProxy.class);
	}

	/**
	 * get all ProteinReference
	 * @return all ProteinReference
	 */
	public Set<ProteinReference> getProteinReferenceList() {
		return getAllOneClassList(ProteinReference.class, ProteinReferenceProxy.class);
	}

	/**
	 * get all RnaReference
	 * @return all RnaReference
	 */
	public Set<RnaReference> getRnaReferenceList() {
		return getAllOneClassList(RnaReference.class, RnaReferenceProxy.class);
	}

	/**
	 * get all SmallMoleculeReference
	 * @return all SmallMoleculeReference
	 */
	public Set<SmallMoleculeReference> getSmallMoleculeReferenceList() {
		return getAllOneClassList(SmallMoleculeReference.class, SmallMoleculeReferenceProxy.class);
	}

	/**
	 * get EntityReference by Name
	 * @return EntityReference
	 */
	public Set<EntityReference> getEntityReferenceListByName(String regex) {
		return getOneClassByName(EntityReference.class, regex);
	}

	/**
	 * get SequenceEntityReference by Name
	 * @param regex
	 * @return SequenceEntityReference
	 */
	public Set<SequenceEntityReference> getSequenceEntityReferenceListByOrganism(String regex) {
		Set result = new HashSet();
		if (session.setup() == false) {
			return result;
		}
		EntityManager em = session.getEntityManager();
		KeywordSearch ks = createKeywordSearch();
		List<BioPAXElement> bss = ks.fetch(em, BioPAXElementProxy.SEARCH_FIELD_NAME, regex, BioSource.class);
		for (BioPAXElement bs : bss) {
			List ps = em.createQuery(
					"select o from " + SequenceEntityReferenceProxy.class.getName() + " as o where o.Organism.RDFId = '" +
					bs.getRDFId() + "'").getResultList();
			if (ps != null) {
				result.addAll(ps);
			}
		}
		return result;
	}

	/**
	 * get SequenceEntityReference by Sequence
	 * @param sequence
	 * @param bExactMatch
	 * @return SequenceEntityReference
	 */
	public Set<SequenceEntityReference> getSequenceEntityReferenceListBySequence(String sequence, boolean bExactMatch) {
		Set result = new HashSet();
		if (session.setup() == false) {
			return result;
		}
		EntityManager em = session.getEntityManager();
		KeywordSearch ks = createKeywordSearch();
		// Luceneでは部分文字列検索がきちんと表現できない。
		// 2007.08.03 Takeshi Yoneki
		String keyword = bExactMatch ? sequence : sequence + "*";
		result.addAll(ks.fetch(em, BioPAXElementProxy.SEARCH_FIELD_SEQUENCE, keyword, SequenceEntityReference.class));
		return result;
	}
}
