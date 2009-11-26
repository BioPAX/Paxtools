/*
 * PathwaySearch.java
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
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.proxy.level3.*;

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
	 * get all Pathway
	 * @return all Pathway
	 */
	public Set<Pathway> getPathwayList() {
		return getAllOneClassList(Pathway.class, PathwayProxy.class);
	}

	/**
	 * get Pathway by Name (level 3 is different)
	 * @param regex
	 * @return set of Pathway
	 */
	public Set<Pathway> getPathwayListByName(String regex) {
		return getOneClassByName(Pathway.class, regex);
	}

	/**
	 * get Pathway by Organism
	 * @param regex
	 * @return set of Pathway
	 */
	public Set<Pathway> getPathwayListByOrganism(String regex) {
		Set result = new HashSet();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		KeywordSearch ks = createKeywordSearch();
		List<BioPAXElement> bss = ks.fetch(em, BioPAXElementProxy.SEARCH_FIELD_NAME, regex, BioSource.class);
		for (BioPAXElement bs: bss) {
			List ps = em.createQuery(
				"select o from " + PathwayProxy.class.getName() + " as o where o.Organism.RDFId = '" + 
					bs.getRDFId() + "'").getResultList();
			if (ps != null)
				result.addAll(ps);
		}
		return result;
	}

	/**
	 * get super pathway
	 * @param p Pathway sub pathway
	 * @return super pathway
	 */
	public Set<Pathway> getSuperPathwayList(Pathway p) {
		Set<Pathway> result = new HashSet<Pathway>();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		result = fetchSuperPathway(em, p);
		return result;
	}

	// 入力Processの親を得る。
	protected Set<Pathway> fetchSuperPathway(EntityManager em, Process inProcess) {
		Set<Pathway> result = new HashSet<Pathway>();
		// 入力ProcessをPathwayComponent(PATHWAY_COMPONENTS)に抱えるPathway。
		Set<Pathway> pSet = fetchPathwayByProcess(em, inProcess);
		if (pSet != null) {
			for (Pathway p: pSet) {
				result.add(p);
			}
		}
		// 入力ProcessをStepProcess(STEP_INTERACTIONS)に抱えるPathwayStep。
		if (inProcess instanceof Process) {
			Set<PathwayStep> psSet = fetchPathwayStepByProcess(em, (Process)inProcess);
			if (psSet != null) {
				for (PathwayStep ps: psSet) {
					Set<Pathway> p2Set = fetchPathwayByPathwayStep(em, ps);
					if (p2Set != null) {
						for (Pathway p: p2Set) {
							result.add(p);
						}
					}
				}
			}
		}
		return result;
	}

	// 入力ProcessをPathwayComponent(PATHWAY_COMPONENTS)に抱えるPathway。
	protected Set<Pathway> fetchPathwayByProcess(EntityManager em, Process inProcess) {
		Set result = new HashSet();
		if (inProcess instanceof Process) {
			List pcs = em.createQuery(
				"select o from " + PathwayProxy.class.getName() + " as o join o.PathwayComponent as p where p.RDFId = '" + 
					inProcess.getRDFId() + "'").getResultList();
			if (pcs != null)
				result.addAll(pcs);
		}
		return result;
	}

	// 入力ProcessをStepProcess(STEP_INTERACTIONS)に抱えるPathwayStep。
	// （PathwayはProcessを継承）
	protected Set<PathwayStep> fetchPathwayStepByProcess(EntityManager em, Process inProcess) {
		Set result = new HashSet();
		if (inProcess instanceof Process) {
			List sis = em.createQuery(
				"select o from " + PathwayStepProxy.class.getName() + " as o join o.StepProcess as p where p.RDFId = '" + 
					inProcess.getRDFId() + "'").getResultList();
			if (sis != null)
				result.addAll(sis);
		}
		return result;
	}

	// 入力PathwayStepをPathwayOrder(PATHWAY_ORDER)に抱えるPathway。
	protected Set<Pathway> fetchPathwayByPathwayStep(EntityManager em, PathwayStep inPathwayStep) {
		Set result = new HashSet();
		if (inPathwayStep instanceof PathwayStep) {
			List pcs = em.createQuery(
				"select o from " + PathwayProxy.class.getName() + " as o join o.PathwayOrder as p where p.RDFId = '" + 
					inPathwayStep.getRDFId() + "'").getResultList();
			if (pcs != null)
				result.addAll(pcs);
		}
		return result;
	}

	/**
	 * get all super Pathway
	 * @param p Pathway sub Pathway
	 * @return all super Pathway
	 */
	public Set<Pathway> getAllSuperPathwayList(Pathway p) {
		HashSet<Pathway> result = new HashSet<Pathway>();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		fetchAllSuperPathway(em, p, result);
		return result;
	}

	protected void fetchAllSuperPathway(EntityManager em, Pathway p, HashSet<Pathway> result) {
		Set<Pathway> sps = fetchSuperPathway(em, p);
		result.addAll(sps);
		for (Pathway sp: sps) {
			fetchAllSuperPathway(em, sp, result);
		}
	}

	/**
	 * get all sub Pathway
	 * @param p Pathway
	 * @return all sub Pathway
	 */
	public Set<Pathway> getAllSubPathwayList(Pathway p) {
		HashSet<Pathway> result = new HashSet<Pathway>();
		fetchAllSubPathway(p, result);
		return result;
	}

///////////////////////////////
// 実装途中
// 2008.03.03 Takeshi Yoneki

	// PathwayComponent(PATHWAY_COMPONENTS)に格納してあるProcessを返す。
	// PathwayOrder経由のStepProcessに格納してあるProcessも加える。
	protected Set<Process> fetchChildProcess(Pathway p) {
		// 親子関係の子方向は構造上オブジェクトが情報を持っている。
		HashSet<Process> result = new HashSet<Process>();
		Set<Process> pcs = p.getPathwayComponent();
		if (pcs != null) {
			result.addAll(pcs);
		}
		Set<PathwayStep> pos = p.getPathwayOrder();
		if (pos != null) {
			for (PathwayStep po: pos) {
			    Set<Process> sps = po.getStepProcess();
				result.addAll(sps);
			}
		}
		return result;
	}

	protected void fetchAllSubPathway(Pathway p, HashSet<Pathway> result) {
		Set<Process> spc = fetchChildProcess(p);
		if (spc != null) {
			for (Process pc: spc) {
				if (pc instanceof Pathway)
					result.add((Pathway)pc);
			}
			for (Process pc: spc) {
				if (pc instanceof Pathway)
					fetchAllSubPathway((Pathway)pc, result);
			}
		}
	}

	/**
	 * get top level Pathway
	 * @param p Pathway
	 * @return top level Pathway
	 */
	public Pathway getTopLevelPathway(Pathway p) {
		if (session.setup() == false)
			return null;
		EntityManager em = session.getEntityManager();
		Pathway top = p;
		while (true) {
			Set<Pathway> pps = fetchSuperPathway(em, top);
			if (pps == null || pps.size() == 0)
				break;
			for (Pathway pp: pps) {
				top = pp;
				break;
			}
		}
		return top;
	}

	/**
	 * get NextStep of Pathway
	 * @param p pathway
	 * @return pathwayStep
	 */
	public Set<PathwayStep> getNextStepListOfPathway(Pathway p) {
		Set<PathwayStep> result = new HashSet<PathwayStep>();
		if (session.setup() == false)
			return result;
		result =  fetchNextStepOfPathway(p);
		return result;
	}

	protected Set<PathwayStep> fetchNextStepOfPathway(Pathway p) {
		HashSet<PathwayStep> result = new HashSet<PathwayStep>();
		Set<PathwayStep> pos = p.getPathwayOrder();
		if (pos != null) {
			for (PathwayStep po: pos) {
			    Set<PathwayStep> nss = po.getNextStep();
				if (nss != null)
					result.addAll(nss);
			}
		}
		return result;
	}

	/**
	 * get all NextStep of Pathway
	 * @param p Pathway
	 * @return PathwayStep
	 */
	public Set<PathwayStep> getAllNextStepListOfPathway(Pathway p) {
		HashSet<PathwayStep> result = new HashSet<PathwayStep>();
		if (session.setup() == false)
			return result;
		fetchAllNextStepOfPathway(p, result);
		return result;
	}

	protected void fetchAllNextStepOfPathway(Pathway p, HashSet<PathwayStep> result) {
		Set<PathwayStep> nss = fetchNextStepOfPathway(p);
		if (nss != null)
			result.addAll(nss);
		Set<Process> cps = fetchChildProcess(p);
		for (Process cp: cps) {
			if (cp instanceof Pathway) {
				fetchAllNextStepOfPathway((Pathway)cp, result);
			}
		}
	}

	/**
	 * get previous steps of Pathway
	 * @param p pathway
	 * @return pathwayStep
	 */
	public Set<PathwayStep> getPreviousStepListOfPathway(Pathway p) {
		Set<PathwayStep> result = new HashSet<PathwayStep>();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		result = fetchPreviousStepOfPathway(em, p);
		return result;
	}

	protected Set<PathwayStep> fetchPreviousStepOfPathway(EntityManager em, Pathway p) {
		HashSet<PathwayStep> result = new HashSet<PathwayStep>();
		Set<PathwayStep> pos = p.getPathwayOrder();
		if (pos != null) {
			for (PathwayStep po: pos) {
				Set<PathwayStep> pss = fetchBackStep(em, (PathwayStep)po);
				if (pss != null)
					result.addAll(pss);
			}
		}
		return result;
	}

	protected Set<PathwayStep> fetchBackStep(EntityManager em, PathwayStep inElem) {
		Set result = new HashSet();
		List nss = em.createQuery(
			"select o from " + PathwayStepProxy.class.getName() + " as o join o.NextStep as p where p.RDFId = '" + 
				inElem.getRDFId() + "'").getResultList();
		if (nss != null)
			result.addAll(nss);
		return result;
	}

	/**
	 * get all previous steps of Pathway
	 * @param p pathway
	 * @return pathwayStep
	 */
	public Set<PathwayStep> getAllPreviousStepListOfPathway(Pathway p) {
		HashSet<PathwayStep> result = new HashSet<PathwayStep>();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		fetchAllPreviousStepOfPathway(em, p, result);
		return result;
	}

	protected void fetchAllPreviousStepOfPathway(EntityManager em, Pathway p, HashSet<PathwayStep> result) {
		Set<PathwayStep> pss = fetchPreviousStepOfPathway(em, p);
		if (pss != null)
			result.addAll(pss);
		Set<Process> cps = fetchChildProcess(p);
		for (Process cp: cps) {
			if (cp instanceof Pathway) {
				fetchAllPreviousStepOfPathway(em, (Pathway)cp, result);
			}
		}
	}

	/**
	 * get EC_NUMBER in pathway
	 * @param p pathway
	 * @return EC_NUMBER
	 */
	public Set<String> getECNumberListInPathway(Pathway p) {
		HashSet<String> result = new HashSet<String>();
		if (session.setup() == false)
			return result;
		fetchECNumberListInPathway(p, result);
		return result;
	}

	protected void fetchECNumberListInPathway(Pathway pc, HashSet<String> result) {
		Set<Process> cps = fetchChildProcess(pc);
		for (Process cp: cps) {
			if (cp instanceof BiochemicalReaction) {
				Set<String> ecns = ((BiochemicalReaction)cp).getECNumber();
				if (ecns != null) {
					result.addAll(ecns);
				}
			}
		}
		for (Process cp: cps) {
			if (cp instanceof Pathway) {
				fetchECNumberListInPathway((Pathway)pc, result);
			}
		}
	}

	/**
	 * get PhysicalEntity in Pathway
	 * @param p Pathway
	 * @return PhysicalEntity
	 */
	public Set<Entity> getPhysicalEntityListInPathway(Pathway p) {
		HashSet<Entity> result = new HashSet<Entity>();
		if (session.setup() == false)
			return result;
		fetchEntity(p, result);
		return result;
	}

    /**
     * TODO add code for Gene!
     *
     * @param e
     * @param result
     */
	protected void fetchEntity(BioPAXElement e, HashSet<Entity> result) {
		if (e instanceof PhysicalEntity) {
			result.add((PhysicalEntity)e);
		}

        if (e instanceof Gene) {
			result.add((Gene)e);
		}

		// 構造の追跡関連
		if (e instanceof Pathway) {
			Set<Process> pcs = ((Pathway)e).getPathwayComponent();
			if (pcs != null) {
				for (Process pc: pcs) {
					fetchEntity(pc, result);
				}
			}
			Set<PathwayStep> pos = ((Pathway)e).getPathwayOrder();
			if (pos != null) {
				for (PathwayStep po: pos) {
					fetchEntity(po, result);
				}
			}
		}
		if (e instanceof PathwayStep) {
		    Set<Process> sps = ((PathwayStep)e).getStepProcess();
		    if (sps != null) {
				for (Process sp: sps) {
					fetchEntity(sp, result);
				}
		    }
		}

		// 値の追加関連
		if (e instanceof Control) {
			Set<PhysicalEntity> pes = ((Control)e).getController();
			if (pes != null) {
				for (PhysicalEntity pe: pes) {
					fetchEntity(pe, result);
				}
			}
		}
		if (e instanceof Catalysis) {
			Set<PhysicalEntity> pes = ((Catalysis)e).getCofactor();
			if (pes != null) {
				for (PhysicalEntity pe: pes) {
					fetchEntity(pe, result);
				}
			}
		}
		if (e instanceof Complex) {
			Set<PhysicalEntity> pes = ((Complex)e).getComponent();
			if (pes != null) {
				for (PhysicalEntity pe: pes) {
					fetchEntity(pe, result);
				}
			}
		}
		if (e instanceof Conversion) {
			Set<PhysicalEntity> pes = ((Conversion)e).getLeft();
			if (pes != null) {
				for (PhysicalEntity pe: pes) {
					fetchEntity(pe, result);
				}
			}
			pes = ((Conversion)e).getRight();
			if (pes != null) {
				for (PhysicalEntity pe: pes) {
					fetchEntity(pe, result);
				}
			}
		}
		if (e instanceof ExperimentalForm) {
			Entity pe = ((ExperimentalForm)e).getExperimentalFormEntity();
			if (pe != null)
				fetchEntity(pe, result);
		}
/*
		if (e instanceof participantStoichiometry) {
			physicalEntity pe = ((participantStoichiometry)e).getPHYSICAL_ENTITY();
			fetchEntity(pe, result);
		}


		if (e instanceof Process) {
			Set<evidence> evs = ((Process)e).getEVIDENCE();
			for (evidence ev: evs) {
				fetchEntity(ev, result);
			}
		}
		if (e instanceof evidence) {
			Set<experimentalForm> efs = ((evidence)e).getEXPERIMENTAL_FORM();
			for (experimentalForm ef: efs) {
				fetchEntity(ef, result);
			}
		}
*/
	}

	/**
	 * get EntityReference in Pathway
	 * @param p pathway
	 * @return interaction
	 */
	public Set<EntityReference> getEntityReferenceListInPathway(Pathway p) {
		HashSet<EntityReference> result = new HashSet<EntityReference>();
		if (session.setup() == false)
			return result;
		fetchEntityReference(p, result);
		return result;
	}

	protected void fetchEntityReference(Pathway p, HashSet<EntityReference> result) {
// getEntityReferenceがなくなった。
// とりあえずコンパイルを通す。
// 未実装。
// 2008.06.06 Takeshi Yoneki
//		HashSet<PhysicalEntity> pes = new HashSet<PhysicalEntity>();
//		fetchEntity(p, pes);
//		for (PhysicalEntity pe: pes) {
//		    EntityReference er = pe.getEntityReference();
//		    if (er != null)
//		    	result.add(er);
//		}
	}

	/**
	 * get Interaction in Pathway
	 * @param p Pathway
	 * @return Interaction
	 */
	public Set<Interaction> getInteractionListInPathway(Pathway p) {
		HashSet<Interaction> result = new HashSet<Interaction>();
		if (session.setup() == false)
			return result;
		fetchInteraction(p, result);
		return result;
	}

	protected void fetchInteraction(Pathway pc, HashSet<Interaction> result) {
		Set<Process> cps = fetchChildProcess(pc);
		for (Process cp: cps) {
			if (cp instanceof Interaction)
				result.add((Interaction)cp);
			else if (cp instanceof Pathway)
				fetchInteraction((Pathway)cp, result);
		}
	}

	/**
	 * get Pathway by Term of EvidenceCode
	 * @param regex
	 * @return Pathway
	 */
	public Set<Pathway> getPathwayListByTermOfEvidenceCode(String regex) {
		HashSet<Pathway> result = new HashSet<Pathway>();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		Set<ControlledVocabulary> cvs = fetchControlledVocabularyByTerm(em, regex);
		HashSet<Evidence> evs = new HashSet<Evidence>();
		for (ControlledVocabulary cv: cvs) {
			List pList = em.createQuery(
				"select o from " + EvidenceProxy.class.getName() + " as o join o.EvidenceCode as p where p.RDFId = '" + 
					cv.getRDFId() + "'").getResultList();
			if (pList != null)
				evs.addAll(pList);
		}
		HashSet<Pathway> ps = new HashSet<Pathway>();
		for (Evidence ev: evs) {
			List pList = em.createQuery(
				"select o from " + PathwayProxy.class.getName() + " as o join o.Evidence as p where p.RDFId = '" + 
					ev.getRDFId() + "'").getResultList();
			if (pList != null)
				ps.addAll(pList);
		}
		return result;
	}

	protected Set<ControlledVocabulary> fetchControlledVocabularyByTerm(EntityManager em, String term) {
		Set<ControlledVocabulary> result = new HashSet<ControlledVocabulary>();
		KeywordSearch ks = createKeywordSearch();
		List<BioPAXElement> s = ks.fetch(em, BioPAXElementProxy.SEARCH_FIELD_TERM, term, ControlledVocabulary.class);
		for (BioPAXElement o: s)
			result.add((ControlledVocabulary)o);
		return result;
	}

}

