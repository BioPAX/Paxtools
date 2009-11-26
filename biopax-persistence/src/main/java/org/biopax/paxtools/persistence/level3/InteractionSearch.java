/*
 * InteractionSearch.java
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
 * interaction search class
 * @author yoneki
 */
public class InteractionSearch extends BaseSearch {
	/**
	 * construct
	 * @param session
	 */
	public InteractionSearch(HiRDBSession session) {
		super(session);
	}

	/**
	 * get all Interaction
	 * @return all Interaction
	 */
	public Set<Interaction> getInteractionList() {
		return getAllOneClassList(Interaction.class, InteractionProxy.class);
	}

	/**
	 * get all Control
	 * @return all Control
	 */
	public Set<Control> getControlList() {
		return getAllOneClassList(Control.class, ControlProxy.class);
	}

	/**
	 * get all Catalysis
	 * @return all Catalysis
	 */
	public Set<Catalysis> getCatalysisList() {
		return getAllOneClassList(Catalysis.class, CatalysisProxy.class);
	}

	/**
	 * get all Modulation
	 * @return all Modulation
	 */
	public Set<Modulation> getModulationList() {
		return getAllOneClassList(Modulation.class, ModulationProxy.class);
	}

	/**
	 * get all TemplateReactionRegulation
	 * @return all TemplateReactionRegulation
	 */
	public Set<TemplateReactionRegulation> getTemplateReactionRegulationList() {
		return getAllOneClassList(TemplateReactionRegulation.class, TemplateReactionRegulationProxy.class);
	}

	/**
	 * get all Conversion
	 * @return all Conversion
	 */
	public Set<Conversion> getConversionList() {
		return getAllOneClassList(Conversion.class, ConversionProxy.class);
	}

	/**
	 * get all BiochemicalReaction
	 * @return all BiochemicalReaction
	 */
	public Set<BiochemicalReaction> getBiochemicalReactionList() {
		return getAllOneClassList(BiochemicalReaction.class, BiochemicalReactionProxy.class);
	}

	/**
	 * get all TransportWithBiochemicalReaction
	 * @return all TransportWithBiochemicalReaction
	 */
	public Set<TransportWithBiochemicalReaction> getTransportWithBiochemicalReactionList() {
		return getAllOneClassList(TransportWithBiochemicalReaction.class, TransportWithBiochemicalReactionProxy.class);
	}

	/**
	 * get all ComplexAssembly
	 * @return all ComplexAssembly
	 */
	public Set<ComplexAssembly> getComplexAssemblyList() {
		return getAllOneClassList(ComplexAssembly.class, ComplexAssemblyProxy.class);
	}

	/**
	 * get all Degradation
	 * @return all Degradation
	 */
	public Set<Degradation> getDegradationList() {
		return getAllOneClassList(Degradation.class, DegradationProxy.class);
	}

	/**
	 * get all Transport
	 * @return all Transport
	 */
	public Set<Transport> getTransportList() {
		return getAllOneClassList(Transport.class, TransportProxy.class);
	}

	/**
	 * get all GeneticInteraction
	 * @return all GeneticInteraction
	 */
	public Set<GeneticInteraction> getGeneticInteractionList() {
		return getAllOneClassList(GeneticInteraction.class, GeneticInteractionProxy.class);
	}

	/**
	 * get all MolecularInteraction
	 * @return all MolecularInteraction
	 */
	public Set<MolecularInteraction> getMolecularInteractionList() {
		return getAllOneClassList(MolecularInteraction.class, MolecularInteractionProxy.class);
	}

	/**
	 * get all TemplateReaction
	 * @return all TemplateReaction
	 */
	public Set<TemplateReaction> getTemplateReactionList() {
		return getAllOneClassList(TemplateReaction.class, TemplateReactionProxy.class);
	}

	/**
	 * get Interaction by InteractionType(INTERACTION_TYPE)
	 * @param regex
	 * @return Interaction
	 */
	public Set<Interaction> getInteractionListByInteractionType(String regex) {
		Set result = new HashSet();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		result = fetchInteractionListByInteractionType(em, regex);
		return result;
	}

	protected Set<Interaction> fetchInteractionListByInteractionType(EntityManager em, String regex) {
		Set result = new HashSet();
		KeywordSearch ks = createKeywordSearch();
		List<BioPAXElement> cvs = ks.fetch(em, BioPAXElementProxy.SEARCH_FIELD_TERM, regex, ControlledVocabulary.class);
		for (BioPAXElement cv: cvs) {
			List pis = em.createQuery(
				"select o from " + InteractionProxy.class.getName() + " as o join o.InteractionType as p where p.RDFId = '" + 
					cv.getRDFId() + "'").getResultList();
			if (pis != null)
				result.addAll(pis);
		}
		return result;
	}

	/**
	 * get BiochemicalReaction by ECNumber(EC_NUMBER)
	 * @param regex
	 * @return BiochemicalReaction
	 */
	public Set<BiochemicalReaction> getBiochemicalReactionListByECNumber(String regex) {
		Set result = new HashSet();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		KeywordSearch ks = createKeywordSearch();
		List<BioPAXElement> brs = ks.fetch(em, BioPAXElementProxy.SEARCH_FIELD_EC_NUMBER, regex, BiochemicalReaction.class);
		result.addAll(brs);
		return result;
	}

	/**
	 * get Conversion by Term of InteractionType(INTERACTION_TYPE_TERM) and Participant(PARTICIPANTS) (level 3 only)
	 * @param pe
	 * @param regex
	 * @return Conversion
	 */
	public Set<Conversion> getConversionListByPhysicalEntityAndInteractionTypeTerm(PhysicalEntity pe, String regex) {
		Set result = new HashSet();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		KeywordSearch ks = createKeywordSearch();
		Set<BioPAXElement> cnvs1 = new HashSet<BioPAXElement>();
		List<BioPAXElement> cvs = ks.fetch(em, BioPAXElementProxy.SEARCH_FIELD_TERM, regex, ControlledVocabulary.class);
		for (BioPAXElement cv: cvs) {
			List cs = em.createQuery(
				"select o from " + ConversionProxy.class.getName() + " as o join o.InteractionType as p where p.RDFId = '" + 
					cv.getRDFId() + "'").getResultList();
			if (cs != null)
				cnvs1.addAll(cs);
		}
		if (cnvs1 == null || cnvs1.size() == 0)
			return result;
		List cnvs2 = em.createQuery(
			"select o from " + ConversionProxy.class.getName() + " as o join o.Participant as p where p.RDFId = '" + 
				pe.getRDFId() + "'").getResultList();
		if (cnvs2 == null || cnvs2.size() == 0)
			return result;
		for (BioPAXElement c1: cnvs1) {
			for (BioPAXElement c2: (List<BioPAXElement>)cnvs2) {
				if (c1.getRDFId().equals(c2.getRDFId()))
					result.add(c1);
			}
		}
		return result;
	}

	/**
	 * get Control by Controller (level 3 only)
	 * @param regex
	 * @return Control
	 */
	public Set<Control> getControlListByController(String regex) {
		Set<Control> result = new HashSet<Control>();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		result = fetchControlByController(em, regex);
		return result;
	}

	//////////////////////////////////////////////////////////////////////
	// 実装途中
	// 現状モデルにおいてControl.ControllerはPhysicalEntityのみで、Pathwayは対象となっていない。
	// biopax-level3.owlではPathwayも対象なのでなんらかの解決が必要。
	// 2008.03.03
	protected Set<Control> fetchControlByController(EntityManager em, String regex) {
		Set result = new HashSet();
		Set<PhysicalEntity> pes = fetchOneClassByName(em, PhysicalEntity.class, regex);
		for (PhysicalEntity pe: pes) {
			List cs = em.createQuery(
				"select o from " + ControlProxy.class.getName() + " as o join o.Controller as p where p.RDFId = '" + 
					pe.getRDFId() + "'").getResultList();
			if (cs != null)
				result.addAll(cs);
		}
		return result;
	}

	/**
	 * get Controlled By Controller (level 3 only)
	 * @param regex
	 * @return Process
	 */
	public Set<Process> getControlledListByController(String regex) {
		Set<Process> result = new HashSet<Process>();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		Set<Control> cs = fetchControlByController(em, regex);
		for (Control c: cs) {
			result.addAll(c.getControlled());
		}
		return result;
	}
}

