/*
 * PathwaySearch.java
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
import org.biopax.paxtools.model.level2.XReferrable;
import org.biopax.paxtools.model.level2.bioSource;
import org.biopax.paxtools.model.level2.biochemicalReaction;
import org.biopax.paxtools.model.level2.catalysis;
import org.biopax.paxtools.model.level2.complex;
import org.biopax.paxtools.model.level2.confidence;
import org.biopax.paxtools.model.level2.control;
import org.biopax.paxtools.model.level2.conversion;
import org.biopax.paxtools.model.level2.dataSource;
import org.biopax.paxtools.model.level2.entity;
import org.biopax.paxtools.model.level2.evidence;
import org.biopax.paxtools.model.level2.experimentalForm;
import org.biopax.paxtools.model.level2.interaction;
import org.biopax.paxtools.model.level2.openControlledVocabulary;
import org.biopax.paxtools.model.level2.pathway;
import org.biopax.paxtools.model.level2.pathwayComponent;
import org.biopax.paxtools.model.level2.pathwayStep;
import org.biopax.paxtools.model.level2.physicalEntity;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level2.physicalInteraction;
import org.biopax.paxtools.model.level2.process;
import org.biopax.paxtools.model.level2.publicationXref;
import org.biopax.paxtools.model.level2.relationshipXref;
import org.biopax.paxtools.model.level2.sequenceEntity;
import org.biopax.paxtools.model.level2.sequenceFeature;
import org.biopax.paxtools.model.level2.sequenceParticipant;
import org.biopax.paxtools.model.level2.unificationXref;
import org.biopax.paxtools.model.level2.xref;
import org.biopax.paxtools.proxy.level2.BioPAXElementProxy;
import org.biopax.paxtools.proxy.level2.evidenceProxy;
import org.biopax.paxtools.proxy.level2.pathwayProxy;
import org.biopax.paxtools.proxy.level2.pathwayStepProxy;
import org.biopax.paxtools.proxy.level2.processProxy;

/**
 * pathway search class
 * @author yoneki
 */
public class PathwaySearch extends BaseSearch {
	/**
	 * construct
	 * @param session
	 */
	public PathwaySearch(HiRDBSession session) {
		super(session);
	}

	/**
	 * get all pathway
	 * @return all pathway
	 */
	public Set<pathway> getPathwayList() {
		return getAllOneClassList(pathway.class, pathwayProxy.class);
	}

	/**
	 * get pathway by NAME
	 * @param regex
	 * @param bIncludeSynonyms
	 * @return pathway
	 */
	public Set<pathway> getPathwayListByNAME(String regex, boolean bIncludeSynonyms) {
		return getOneClassByNAME(pathway.class, regex, bIncludeSynonyms);
	}

	/**
	 * get pathway by ORGANISM
	 * @param regex
	 * @return pathway
	 */
	public Set<pathway> getPathwayListByORGANISM(String regex) {
		Set result = new HashSet();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		KeywordSearch ks = session.createKeywordSearch();
		List<BioPAXElement> bss = ks.fetch(em, BioPAXElementProxy.SEARCH_FIELD_NAME, regex, bioSource.class);
		for (BioPAXElement bs: bss) {
			List ps = em.createQuery(
				"select o from " + pathwayProxy.class.getName() + " as o where o.ORGANISM.RDFId = '" + 
					bs.getRDFId() + "'").getResultList();
			if (ps != null)
				result.addAll(ps);
		}
		return result;
	}

	/**
	 * get super pathway
	 * @param pathway sub pathway
	 * @return super pathway
	 */
	public Set<pathway> getSuperPathwayList(pathway p) {
		Set<pathway> result = new HashSet<pathway>();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		result = fetchSuperPathway(em, p);
		return result;
	}

	// 入力pathwayComonentの親を得る。
	// （pathwayはpathwayComponentを継承）
	protected Set<pathway> fetchSuperPathway(EntityManager em, pathwayComponent inPC) {
		Set<pathway> result = new HashSet<pathway>();
		// 入力pathwayComonentをPATHWAY_COMPONENTSに抱えるpathway。
		Set<pathway> pSet = fetchPathwayByPathwayComonent(em, inPC);
		if (pSet != null) {
			for (pathway p: pSet) {
				result.add(p);
			}
		}
		// 入力processをSTEP_INTERACTIONSに抱えるpathwayStep。
		if (inPC instanceof process) {
			Set<pathwayStep> psSet = fetchPathwayStepByProcess(em, (process)inPC);
			if (psSet != null) {
				for (pathwayStep ps: psSet) {
					Set<pathway> p2Set = fetchPathwayByPathwayComonent(em, ps);
					if (p2Set != null) {
						for (pathway p: p2Set) {
							result.add(p);
						}
					}
				}
			}
		}
		return result;
	}

	// 入力processをSTEP_INTERACTIONSに抱えるpathwayStep。
	// （pathwayはprocessを継承）
	protected Set<pathwayStep> fetchPathwayStepByProcess(EntityManager em, process inProcess) {
		Set result = new HashSet();
		if (inProcess instanceof process) {
			List sis = em.createQuery(
				"select o from " + pathwayStepProxy.class.getName() + " as o join o.STEP_INTERACTIONS as p where p.RDFId = '" + 
					inProcess.getRDFId() + "'").getResultList();
			if (sis != null)
				result.addAll(sis);
		}
		return result;
	}

	// 入力pathwayComonentをPATHWAY_COMPONENTSに抱えるpathway。
	protected Set<pathway> fetchPathwayByPathwayComonent(EntityManager em, pathwayComponent inPC) {
		Set result = new HashSet();
		if (inPC instanceof pathwayComponent) {
			List ps = em.createQuery(
				"select o from " + pathwayProxy.class.getName() + " as o join o.PATHWAY_COMPONENTS as p where p.RDFId = '" + 
					inPC.getRDFId() + "'").getResultList();
			if (ps != null)
				result.addAll(ps);
		}
		return result;
	}

	/**
	 * get all super pathway
	 * @param pathway sub pathway
	 * @return all super pathway
	 */
	public Set<pathway> getAllSuperPathwayList(pathway p) {
		HashSet<pathway> result = new HashSet<pathway>();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		fetchAllSuperPathway(em, p, result);
		return result;
	}

	protected void fetchAllSuperPathway(EntityManager em, pathway p, HashSet<pathway> result) {
		Set<pathway> sps = fetchSuperPathway(em, p);
		result.addAll(sps);
		for (pathway sp: sps) {
			fetchAllSuperPathway(em, sp, result);
		}
	}

	/**
	 * get all sub pathway
	 * @param p pathway
	 * @return all sub pathway
	 */
	public Set<pathway> getAllSubPathwayList(pathway p) {
		HashSet<pathway> result = new HashSet<pathway>();
		fetchAllSubPathway(p, result);
		return result;
	}

	// PATHWAY_COMPONENTSに格納してある値またはSTEP_INTERACTIONSのpathwayを返す。
	protected Set<pathwayComponent> fetchSubPathwayComponent(pathwayComponent inElem) {
		// 親子関係の子方向は構造上オブジェクトが情報を持っている。
		Set<pathwayComponent> result = new HashSet<pathwayComponent>();
		if (inElem instanceof pathway) {
			Set<pathwayComponent> pcSet = ((pathway)inElem).getPATHWAY_COMPONENTS();
			if (pcSet != null) {
				for (pathwayComponent pc: pcSet) {
					result.add(pc);
				}
			}
		}
		else if (inElem instanceof pathwayStep) {
			Set<process> siSet = ((pathwayStep)inElem).getSTEP_INTERACTIONS();
			if (siSet != null) {
				for (process si: siSet) {
					if (si instanceof pathway) {
						result.add((pathway)si);
					}
				}
			}
		}
		else {
		}
		return result;
	}

	// PATHWAY_COMPONENTSに格納してあるpathwayComponentまたはSTEP_INTERACTIONSのprocessを返す。
	protected Set<pathwayComponent> fetchChildPathwayComponentOrProcess(pathwayComponent inElem) {
		// 親子関係の子方向は構造上オブジェクトが情報を持っている。
		Set<pathwayComponent> result = new HashSet<pathwayComponent>();
		if (inElem instanceof pathway) {
			Set<pathwayComponent> pcSet = ((pathway)inElem).getPATHWAY_COMPONENTS();
			if (pcSet != null) {
				for (pathwayComponent pc: pcSet) {
					result.add(pc);
				}
			}
		}
		else if (inElem instanceof pathwayStep) {
			Set<process> siSet = ((pathwayStep)inElem).getSTEP_INTERACTIONS();
			if (siSet != null) {
				for (process si: siSet) {
					result.add(si);
				}
			}
		}
		else {
		}
		return result;
	}

	protected void fetchAllSubPathway(pathwayComponent p, HashSet<pathway> result) {
		Set<pathwayComponent> spc = fetchSubPathwayComponent(p);
		for (pathwayComponent pc: spc) {
			if (pc instanceof pathway)
				result.add((pathway)pc);
		}
		for (pathwayComponent pc: spc) {
			fetchAllSubPathway(pc, result);
		}
	}

	/**
	 * get top level pathway
	 * @param p pathway
	 * @return top level pathway
	 */
	public pathway getTopLevelPathway(pathway p) {
		if (session.setup() == false)
			return null;
		EntityManager em = session.getEntityManager();
		pathway top = p;
		while (true) {
			Set<pathway> pps = fetchSuperPathway(em, top);
			if (pps.size() == 0)
				break;
			for (pathway pp: pps) {
				top = pp;
				break;
			}
		}
		return top;
	}

	/**
	 * get NEXT_STEP of pathway
	 * @param p pathway
	 * @return pathwayStep
	 */
	public Set<pathwayStep> getNEXT_STEPListOfPathway(pathway p) {
		Set<pathwayStep> result = new HashSet<pathwayStep>();
		if (session.setup() == false)
			return result;
		result =  fetchNEXT_STEPOfPathway(p);
		return result;
	}

	protected Set<pathwayStep> fetchNEXT_STEPOfPathway(pathway p) {
		HashSet<pathwayStep> result = new HashSet<pathwayStep>();
		Set<pathwayComponent> pcSet = p.getPATHWAY_COMPONENTS();
		for (pathwayComponent pc: pcSet) {
			if (pc instanceof pathwayStep) {
				Set<pathwayStep> psSet = ((pathwayStep)pc).getNEXT_STEP();
				if (psSet != null)
					result.addAll(psSet);
			}
		}
		return result;
	}

	/**
	 * get all NEXT_STEP of pathway
	 * @param p pathway
	 * @return pathwayStep
	 */
	public Set<pathwayStep> getAllNEXT_STEPListOfPathway(pathway p) {
		HashSet<pathwayStep> result = new HashSet<pathwayStep>();
		if (session.setup() == false)
			return result;
		fetchAllNEXT_STEPOfPathway(p, result);
		return result;
	}

	protected void fetchAllNEXT_STEPOfPathway(pathwayComponent p, HashSet<pathwayStep> result) {
		if (p instanceof pathway) {
			Set<pathwayStep> psSet = fetchNEXT_STEPOfPathway((pathway)p);
			result.addAll(psSet);
		}
		Set<pathwayComponent> spc = fetchChildPathwayComponentOrProcess(p);
		for (pathwayComponent pc: spc) {
			fetchAllNEXT_STEPOfPathway(pc, result);
		}
	}

	/**
	 * get previous steps of pathway
	 * @param p pathway
	 * @return pathwayStep
	 */
	public Set<pathwayStep> getPreviousStepListOfPathway(pathway p) {
		Set<pathwayStep> result = new HashSet<pathwayStep>();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		result = fetchPreviousStepOfPathway(em, p);
		return result;
	}

	protected Set<pathwayStep> fetchPreviousStepOfPathway(EntityManager em, pathway p) {
		Set<pathwayStep> result = new HashSet<pathwayStep>();
		Set<pathwayComponent> pcSet = p.getPATHWAY_COMPONENTS();
		for (pathwayComponent pc: pcSet) {
			if (pc instanceof pathwayStep) {
				Set<pathwayStep> psSet = fetchBackStep(em, (pathwayStep)pc);
				result.addAll(psSet);
			}
		}
		return result;
	}

	protected Set<pathwayStep> fetchBackStep(EntityManager em, pathwayStep inElem) {
		Set result = new HashSet();
		List nss = em.createQuery(
			"select o from " + pathwayStepProxy.class.getName() + " as o join o.NEXT_STEP as p where p.RDFId = '" + 
				inElem.getRDFId() + "'").getResultList();
		if (nss != null)
			result.addAll(nss);
		return result;
	}

	/**
	 * get all previous steps of pathway
	 * @param p pathway
	 * @return pathwayStep
	 */
	public Set<pathwayStep> getAllPreviousStepListOfPathway(pathway p) {
		HashSet<pathwayStep> result = new HashSet<pathwayStep>();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		fetchAllPreviousStepOfPathway(em, p, result);
		return result;
	}

	protected void fetchAllPreviousStepOfPathway(EntityManager em, pathwayComponent p, HashSet<pathwayStep> result) {
		if (p instanceof pathway) {
			Set<pathwayStep> psSet = fetchPreviousStepOfPathway(em, (pathway)p);
			result.addAll(psSet);
		}
		Set<pathwayComponent> spc = fetchChildPathwayComponentOrProcess(p);
		for (pathwayComponent pc: spc) {
			fetchAllPreviousStepOfPathway(em, pc, result);
		}
	}

	/**
	 * get EC_NUMBER in pathway
	 * @param p pathway
	 * @return EC_NUMBER
	 */
	public Set<String> getEC_NUMBERListInPathway(pathway p) {
		HashSet<String> result = new HashSet<String>();
		if (session.setup() == false)
			return result;
		fetchEC_NUMBERInPathwayComponent(p, result);
		return result;
	}

	protected void fetchEC_NUMBERInPathwayComponent(pathwayComponent pc, HashSet<String> result) {
		if (pc instanceof biochemicalReaction) {
			Set<String> ens = ((biochemicalReaction)pc).getEC_NUMBER();
			if (ens != null)
				result.addAll(ens);
			return;
		}
		Set<pathwayComponent> spcs = fetchChildPathwayComponentOrProcess(pc);
		for (pathwayComponent spc: spcs) {
			fetchEC_NUMBERInPathwayComponent(spc, result);
		}
	}

	/**
	 * get physicalEntity in pathway
	 * @param p pathway
	 * @return physicalEntity
	 */
	public Set<physicalEntity> getPhysicalEntityListInPathway(pathway p) {
		HashSet<physicalEntity> result = new HashSet<physicalEntity>();
		if (session.setup() == false)
			return result;
		fetchPhysicalEntity(p, result);
		return result;
	}

	protected void fetchPhysicalEntity(BioPAXElement e, HashSet<physicalEntity> result) {
		if (e instanceof physicalEntityParticipant) {
			physicalEntity pe = ((physicalEntityParticipant)e).getPHYSICAL_ENTITY();
			fetchPhysicalEntity(pe, result);
		}
		if (e instanceof physicalEntity) {
			result.add((physicalEntity)e);
		}
		if (e instanceof complex) {
			Set<physicalEntityParticipant> cs = ((complex)e).getCOMPONENTS();
			for (physicalEntityParticipant c: cs) {
				fetchPhysicalEntity(c, result);
			}
		}

		if (e instanceof control) {
			Set<physicalEntityParticipant> cs = ((control)e).getCONTROLLER();
			for (physicalEntityParticipant c: cs) {
				fetchPhysicalEntity(c, result);
			}
		}
		if (e instanceof catalysis) {
			Set<physicalEntityParticipant> cs = ((catalysis)e).getCOFACTOR();
			for (physicalEntityParticipant c: cs) {
				fetchPhysicalEntity(c, result);
			}
		}
		if (e instanceof conversion) {
			Set<physicalEntityParticipant> ls = ((conversion)e).getLEFT();
			for (physicalEntityParticipant l: ls) {
				fetchPhysicalEntity(l, result);
			}
			Set<physicalEntityParticipant> rs = ((conversion)e).getRIGHT();
			for (physicalEntityParticipant r: rs) {
				fetchPhysicalEntity(r, result);
			}
		}

		if (e instanceof process) {
			Set<evidence> evs = ((process)e).getEVIDENCE();
			for (evidence ev: evs) {
				fetchPhysicalEntity(ev, result);
			}
		}
		if (e instanceof evidence) {
			Set<experimentalForm> efs = ((evidence)e).getEXPERIMENTAL_FORM();
			for (experimentalForm ef: efs) {
				fetchPhysicalEntity(ef, result);
			}
		}
		if (e instanceof experimentalForm) {
			physicalEntityParticipant p = ((experimentalForm)e).getPARTICIPANT();
			fetchPhysicalEntity(p, result);
		}

		if (e instanceof pathway) {
			Set<pathwayComponent> pcSet = ((pathway)e).getPATHWAY_COMPONENTS();
			for (pathwayComponent pc: pcSet) {
				fetchPhysicalEntity(pc, result);
			}
		}
		if (e instanceof pathwayStep) {
			Set<process> siSet = ((pathwayStep)e).getSTEP_INTERACTIONS();
			for (process si: siSet) {
				fetchPhysicalEntity(si, result);
			}
		}
	}

	/**
	 * get interaction in pathway
	 * @param p pathway
	 * @return interaction
	 */
	public Set<interaction> getInteractionListInPathway(pathway p) {
		HashSet<interaction> result = new HashSet<interaction>();
		if (session.setup() == false)
			return result;
		fetchInteraction(p, result);
		return result;
	}

	protected void fetchInteraction(pathwayComponent pc, HashSet<interaction> result) {
		if (pc instanceof interaction) {
			result.add((interaction)pc);
		}
		Set<pathwayComponent> spcs = fetchChildPathwayComponentOrProcess(pc);
		for (pathwayComponent spc: spcs) {
			fetchInteraction(spc, result);
		}
	}

	/**
	 * get all evidence in pathway
	 * @param p pathway
	 * @return evidence
	 */
	public Set<evidence> getAllEvidenceListInPathway(pathway p) {
		HashSet<evidence> result = new HashSet<evidence>();
		if (session.setup() == false)
			return result;
		fetchEvidence(p, result);
		return result;
	}

	protected void fetchEvidence(pathwayComponent pc, HashSet<evidence> result) {
		if (pc instanceof process) {
			Set<evidence> evs = ((process)pc).getEVIDENCE();
			if (evs != null)
				result.addAll(evs);
		}
		Set<pathwayComponent> spcs = fetchChildPathwayComponentOrProcess(pc);
		for (pathwayComponent spc: spcs) {
			fetchEvidence(spc, result);
		}
	}

	/**
	 * get all publicationXref in pathway
	 * @param p pathway
	 * @return publicationXref
	 */
	Set<publicationXref> getAllPublicationXrefListInPathway_xxx(pathway p) {
		HashSet<publicationXref> result = new HashSet<publicationXref>();
		if (session.setup() == false)
			return result;
		HashSet<xref> xs = new HashSet<xref>();
		fetchXref(p, xs);
		for (xref x: xs) {
			if (x instanceof publicationXref)
				result.add((publicationXref)x);
		}
		return result;
	}

	protected void fetchXref(BioPAXElement e, HashSet<xref> result) {
		if (e instanceof XReferrable) {
			Set<xref> xs = ((XReferrable)e).getXREF();
			if (xs != null)
				result.addAll(xs);
		}
		//
		if (e instanceof physicalEntityParticipant) {
			physicalEntity pe = ((physicalEntityParticipant)e).getPHYSICAL_ENTITY();
			fetchXref(pe, result);
			openControlledVocabulary cl = ((physicalEntityParticipant)e).getCELLULAR_LOCATION();
			fetchXref(cl, result);
		}
		if (e instanceof evidence) {
			Set<confidence> cs = ((evidence)e).getCONFIDENCE();
			for (confidence c: cs)
				fetchXref(c, result);
			Set<openControlledVocabulary> ecs = ((evidence)e).getEVIDENCE_CODE();
			for (openControlledVocabulary ec: ecs)
				fetchXref(ec, result);
			Set<experimentalForm> efs = ((evidence)e).getEXPERIMENTAL_FORM();
			for (experimentalForm ef: efs)
				fetchXref(ef, result);
		}
		if (e instanceof entity) {
			Set<dataSource> dss = ((entity)e).getDATA_SOURCE();
			for (dataSource ds: dss)
				fetchXref(ds, result);
		}
		if (e instanceof process) {
			Set<evidence> evs = ((process)e).getEVIDENCE();
			for (evidence ev: evs)
				fetchXref(ev, result);
		}
		if (e instanceof bioSource) {
			openControlledVocabulary ct = ((bioSource)e).getCELLTYPE();
			fetchXref(ct, result);
			openControlledVocabulary t = ((bioSource)e).getTISSUE();
			fetchXref(t, result);
		}
		if (e instanceof experimentalForm) {
			Set<openControlledVocabulary> efts = ((experimentalForm)e).getEXPERIMENTAL_FORM_TYPE();
			for (openControlledVocabulary eft: efts)
				fetchXref(eft, result);
			physicalEntityParticipant p = ((experimentalForm)e).getPARTICIPANT();
			fetchXref(p, result);
		}
		if (e instanceof physicalInteraction) {
			Set<openControlledVocabulary> its = ((physicalInteraction)e).getINTERACTION_TYPE();
			for (openControlledVocabulary it: its)
				fetchXref(it, result);
		}
		if (e instanceof sequenceFeature) {
			openControlledVocabulary ft = ((sequenceFeature)e).getFEATURE_TYPE();
			fetchXref(ft, result);
		}
		if (e instanceof sequenceParticipant) {
			Set<sequenceFeature> sfl = ((sequenceParticipant)e).getSEQUENCE_FEATURE_LIST();
			for (sequenceFeature sf: sfl)
				fetchXref(sf, result);
		}
		if (e instanceof catalysis) {
			Set<physicalEntityParticipant> cs = ((catalysis)e).getCOFACTOR();
			for (physicalEntityParticipant c: cs)
				fetchXref(c, result);
		}
		if (e instanceof complex) {
			Set<physicalEntityParticipant> cs = ((complex)e).getCOMPONENTS();
			for (physicalEntityParticipant c: cs)
				fetchXref(c, result);
			bioSource o = ((complex)e).getORGANISM();
			fetchXref(o, result);
		}
		if (e instanceof control) {
			Set<physicalEntityParticipant> crs = ((control)e).getCONTROLLER();
			for (physicalEntityParticipant cr: crs)
				fetchXref(cr, result);
			//Set<process> cds = ((control)e).getCONTROLLED();
			//for (process cd: cds)
			//	fetchXref(cd, result);
		}
		if (e instanceof conversion) {
// 禁止されたsetterのため永続化対象外
// 2007.07.31 Takeshi Yoneki
// 対象外解除
// 2007.09.05
			Set<physicalEntityParticipant> ls = ((conversion)e).getLEFT();
			for (physicalEntityParticipant l: ls)
				fetchXref(l, result);
			Set<physicalEntityParticipant> rs = ((conversion)e).getRIGHT();
			for (physicalEntityParticipant r: rs)
				fetchXref(r, result);

		}
		if (e instanceof pathwayStep) {
			Set<process> sis = ((pathwayStep)e).getSTEP_INTERACTIONS();
			for (process si: sis)
				fetchXref(si, result);
			//Set<pathwayStep> nss = ((pathwayStep)e).getNEXT_STEP();
			//for (pathwayStep ns: nss)
			//	fetchXref(ns, result);
		}
		if (e instanceof pathway) {
			bioSource o = ((pathway)e).getORGANISM();
			fetchXref(o, result);
			Set<pathwayComponent> pcs = ((pathway)e).getPATHWAY_COMPONENTS();
			for (pathwayComponent pc: pcs)
				fetchXref(pc, result);
		}
		if (e instanceof sequenceEntity) {
			bioSource o = ((sequenceEntity)e).getORGANISM();
			fetchXref(o, result);
		}
	}

	/**
	 * get all URL in pathway
	 * @param p pathway
	 * @return URL
	 */
	Set<String> getAllURLListInPathway_xxx(pathway p) {
		HashSet<String> result = new HashSet<String>();
		if (session.setup() == false)
			return result;
		HashSet<xref> xs = new HashSet<xref>();
		fetchXref(p, xs);
		for (xref x: xs) {
			if (x instanceof publicationXref) {
				Set<String> urls = ((publicationXref)x).getURL();
				result.addAll(urls);
			}
		}
		return result;
	}

	/**
	 * get all unificationXref in pathway
	 * @param p pathway
	 * @return unificationXref
	 */
	Set<unificationXref> getAllUnificationXrefListInPathway_xxx(pathway p) {
		HashSet<unificationXref> result = new HashSet<unificationXref>();
		if (session.setup() == false)
			return result;
		HashSet<xref> xs = new HashSet<xref>();
		fetchXref(p, xs);
		for (xref x: xs) {
			if (x instanceof unificationXref)
				result.add((unificationXref)x);
		}
		return result;
	}

	/**
	 * get all relationshipXref in pathway
	 * @param p pathway
	 * @return relationshipXref
	 */
	Set<relationshipXref> getAllRelationshipXrefListInPathway_xxx(pathway p) {
		HashSet<relationshipXref> result = new HashSet<relationshipXref>();
		if (session.setup() == false)
			return result;
		HashSet<xref> xs = new HashSet<xref>();
		fetchXref(p, xs);
		for (xref x: xs) {
			if (x instanceof relationshipXref)
				result.add((relationshipXref)x);
		}
		return result;
	}

	/**
	 * get pathway by TERM of EVIDENCE_CODE
	 * @param regex
	 * @return pathway
	 */
	public Set<pathway> getPathwayListByTERMOfEVIDENCE_CODE(String regex) {
		HashSet<pathway> result = new HashSet<pathway>();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		Set<openControlledVocabulary> ocvs = fetchOpenControlledVocabularyByTerm(em, regex);
		HashSet<evidence> evs = new HashSet<evidence>();
		for (openControlledVocabulary ocv: ocvs) {
			List pList = em.createQuery(
				"select o from " + evidenceProxy.class.getName() + " as o join o.EVIDENCE_CODE as p where p.RDFId = '" + 
					ocv.getRDFId() + "'").getResultList();
			if (pList != null)
				evs.addAll(pList);
		}
		HashSet<process> ps = new HashSet<process>();
		for (evidence ev: evs) {
			List pList = em.createQuery(
				"select o from " + processProxy.class.getName() + " as o join o.EVIDENCE as p where p.RDFId = '" + 
					ev.getRDFId() + "'").getResultList();
			if (pList != null)
				ps.addAll(pList);
		}
		for (process p: ps) {
			if (p instanceof pathway)
				result.add((pathway)p);
		}
		return result;
	}

	protected Set<openControlledVocabulary> fetchOpenControlledVocabularyByTerm(EntityManager em, String term) {
		Set<openControlledVocabulary> result = new HashSet<openControlledVocabulary>();
		KeywordSearch ks = session.createKeywordSearch();
		List<BioPAXElement> s = ks.fetch(em, BioPAXElementProxy.SEARCH_FIELD_TERM, term, openControlledVocabulary.class);
		for (BioPAXElement o: s)
			result.add((openControlledVocabulary)o);
		return result;
	}

}

