/*
 * MiscSearch.java
 *
 * 2007.07.20 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.persistence.level2;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.InteractionParticipant;
import org.biopax.paxtools.model.level2.XReferrable;
import org.biopax.paxtools.model.level2.complex;
import org.biopax.paxtools.model.level2.dataSource;
import org.biopax.paxtools.model.level2.entity;
import org.biopax.paxtools.model.level2.interaction;
import org.biopax.paxtools.model.level2.pathway;
import org.biopax.paxtools.model.level2.pathwayComponent;
import org.biopax.paxtools.model.level2.pathwayStep;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level2.process;
import org.biopax.paxtools.model.level2.xref;
import org.biopax.paxtools.proxy.level2.BioPAXElementProxy;
import org.biopax.paxtools.proxy.level2.complexProxy;
import org.biopax.paxtools.proxy.level2.confidenceProxy;
import org.biopax.paxtools.proxy.level2.dataSourceProxy;
import org.biopax.paxtools.proxy.level2.entityProxy;
import org.biopax.paxtools.proxy.level2.evidenceProxy;
import org.biopax.paxtools.proxy.level2.openControlledVocabularyProxy;
import org.biopax.paxtools.proxy.level2.pathwayProxy;
import org.biopax.paxtools.proxy.level2.pathwayStepProxy;
import org.biopax.paxtools.proxy.level2.physicalEntityParticipantProxy;
import org.biopax.paxtools.proxy.level2.sequenceFeatureProxy;

/**
 * misc search class
 * @author yoneki
 */
public class MiscSearch extends BaseSearch {
	/**
	 * construct
	 * @param session
	 */
	public MiscSearch(HiRDBSession session) {
		super(session);
	}

	Set fetchBackPointer(EntityManager em, Class resultProxyClass, String propName, BioPAXElement inElem) {
		Set result = new HashSet();
		List s = em.createQuery(
			//"select o from PathwayStepProxy as o join o.NEXT_STEP as p where p.RDFId = '" +
			"select o from " + resultProxyClass.getName() + " as o join o." + propName + " as p where p.RDFId = '" + 
				inElem.getRDFId() + "'").getResultList();
		if (s != null)
			result.addAll(s);
		return result;
	}

	/**
	 * get back pointer of NEXT_STEP
	 * @param pathwayStep
	 * @return back pointer of NEXT_STEP
	 */
	public Set<pathwayStep> isNEXT_STEPof(pathwayStep ps) {
		Set result = new HashSet();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		result = fetchBackPointer(em, pathwayStepProxy.class, "NEXT_STEP", ps);
		return result;
	}

	/**
	 * get back pointer of PATHWAY_COMPONENT
	 * @param pathwayComponent
	 * @return back pointer of PATHWAY_COMPONENT
	 */
	public Set<pathway> isPATHWAY_COMPONENTSof(pathwayComponent pc) {
		Set result = new HashSet();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		result = fetchBackPointer(em, pathwayProxy.class, "PATHWAY_COMPONENTS", pc);
		return result;
	}

	/**
	 * get back pointer of STEP_INTERACTIONS
	 * @param process
	 * @return back pointer of STEP_INTERACTIONS
	 */
	public Set<pathwayStep> isSTEP_INTERACTIONSof(process p) {
		Set result = new HashSet();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		result = fetchBackPointer(em, pathwayStepProxy.class, "STEP_INTERACTIONS", p);
		return result;
	}

	/**
	 * get back pointer of XREF
	 * @param XReferrable
	 * @return back pointer of XREF
	 */
	public Set<XReferrable> isXREFof(xref x) {
		Set result = new HashSet();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		result.addAll(fetchBackPointer(em, confidenceProxy.class, "XREF", x));
		result.addAll(fetchBackPointer(em, dataSourceProxy.class, "XREF", x));
		result.addAll(fetchBackPointer(em, entityProxy.class, "XREF", x));
		result.addAll(fetchBackPointer(em, evidenceProxy.class, "XREF", x));
		result.addAll(fetchBackPointer(em, openControlledVocabularyProxy.class, "XREF", x));
		result.addAll(fetchBackPointer(em, sequenceFeatureProxy.class, "XREF", x));
		return result;
	}

	public Set<interaction> isPARTICIPANTSof(InteractionParticipant ip) {
		// interactinを継承したクラスでPARTICIPANTSの直接のsettertを禁じているので、Beansを永続化対象としない。
		// 2007.04.26 Takeshi Yoneki
		// よってHiRDBからこの情報を引き出すことはできない。
		// 2007.07.03
		// nullでなく空のSetを返す。
		// 2007.08.30
		//return new HashSet();
		// 直接のsetterを使わない別メソッドで表現することにした。そのため復帰。
		// 2007.09.05
		Set result = new HashSet();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		result = fetchBackPointer(em, interaction.class, "PARTICIPANTS_x", ip);
		return result;
	}

	/**
	 * get back pointer of COMPONENTS
	 * @param physicalEntityParticipant
	 * @return back pointer of COMPONENTS
	 */
	public complex isCOMPONENTSof(physicalEntityParticipant pep) {
		Set result = new HashSet();
		if (session.setup() == false)
			return null;
		EntityManager em = session.getEntityManager();
		result = fetchBackPointer(em, complexProxy.class, "COMPONENTS", pep);
		if (result.size() > 0) {
			for (Object c: result) {
				return (complex)c;
			}
		}
		return null;
	}

	/**
	 * get back pointer of PHYSICAL_ENTITY
	 * @param physicalEntity
	 * @return back pointer of PHYSICAL_ENTITY
	 */
	public Set<physicalEntityParticipant> isPHYSICAL_ENTITYof(physicalEntity pe) {
		Set result = new HashSet();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		result = fetchBackPointer(em, physicalEntityParticipantProxy.class, "PHYSICAL_ENTITY", pe);
		return result;
	}

	/**
	 * get entity by DATA_SOURCE
	 * @param regex
	 * @return entities
	 */
	public Set<entity> getEntityListByDATA_SOURCE(String regex) {
		Set result = new HashSet();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		KeywordSearch ks = session.createKeywordSearch();
		List<BioPAXElement> dss = ks.fetch(em, BioPAXElementProxy.SEARCH_FIELD_NAME, regex, dataSource.class);
		for (BioPAXElement ds: dss) {
			List es = em.createQuery(
				"select o from " + entityProxy.class.getName() + " as o join o.DATA_SOURCE as p where p.RDFId = '" + 
					ds.getRDFId() + "'").getResultList();
			if (es != null)
				result.addAll(es);
		}
		return result;
	}

	/**
	 * get entity by DB and ID of xref
	 * @param regexDB
	 * @param id
	 * @return entities
	 */
	public Set<entity> getEntityListByDBAndIDOfXREF(String regexDB, String id) {
		Set<entity> result = new HashSet<entity>();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		KeywordSearch ks = session.createKeywordSearch();
		HashSet<BioPAXElement> xs = new HashSet<BioPAXElement>();
		HashSet<BioPAXElement> xsdb = new HashSet<BioPAXElement>();
		HashSet<BioPAXElement> xsid = new HashSet<BioPAXElement>();
		xsdb.addAll(ks.fetch(em, BioPAXElementProxy.SEARCH_FIELD_XREF_DB, regexDB, xref.class));
		xsid.addAll(ks.fetch(em, BioPAXElementProxy.SEARCH_FIELD_XREF_ID, id, xref.class));
		if (regexDB == null || regexDB.length() == 0) {
			xs = xsid;
		}
		else if (id == null || id.length() == 0) {
			xs = xsdb;
		}
		else {
			for (BioPAXElement x: xsdb) {
				if (xsid.contains(x)) {
					xs.add(x);
				}
			}
		}
		for (BioPAXElement x: xs) {
			List es = em.createQuery(
				"select o from " + entityProxy.class.getName() + " as o join o.XREF as p where p.RDFId = '" + 
					x.getRDFId() + "'").getResultList();
			if (es != null)
				result.addAll(es);
		}
		return result;
	}

	/**
	 * get entity by NAME
	 * @param regex
	 * @param bIncludeSynonyms
	 * @return entities
	 */
	public Set<entity> getEntityListByNAME(String regex, boolean bIncludeSynonyms) {
		return getOneClassByNAME(entity.class, regex, bIncludeSynonyms);
	}

	/**
	 * get data source
	 * @return data source names
	 */
	public Set<String> getDataSourceList() {
		HashSet<String> result = new HashSet<String>();
		Set dss = getAllOneClassList(dataSource.class, dataSourceProxy.class);
		for (Object ds: dss) {
			Set<String> names = ((dataSource)ds).getNAME();
			result.addAll(names);
		}
		return result;
	}

	/**
	 * get entity by AVAIRABILITY
	 * @param regex
	 * @return entities
	 */
	public Set<entity> getEntityListByAVAIRABILITY(String regex) {
		Set result = new HashSet();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		KeywordSearch ks = session.createKeywordSearch();
		result.addAll(ks.fetch(em, BioPAXElementProxy.SEARCH_FIELD_AVAILABILITY, regex, entity.class));
		return result;
	}

	/**
	 * get entity by COMMENT
	 * @param regex
	 * @return entities
	 */
	public Set<entity> getEntityListByCOMMENT(String regex) {
		Set result = new HashSet();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		KeywordSearch ks = session.createKeywordSearch();
		result.addAll(ks.fetch(em, BioPAXElementProxy.SEARCH_FIELD_COMMENT, regex, entity.class));
		return result;
	}
}

