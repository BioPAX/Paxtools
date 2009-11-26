/*
 * InteractionSearch.java
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
import org.biopax.paxtools.model.level2.biochemicalReaction;
import org.biopax.paxtools.model.level2.catalysis;
import org.biopax.paxtools.model.level2.complexAssembly;
import org.biopax.paxtools.model.level2.control;
import org.biopax.paxtools.model.level2.conversion;
import org.biopax.paxtools.model.level2.interaction;
import org.biopax.paxtools.model.level2.modulation;
import org.biopax.paxtools.model.level2.openControlledVocabulary;
import org.biopax.paxtools.model.level2.physicalEntityParticipant;
import org.biopax.paxtools.model.level2.physicalInteraction;
import org.biopax.paxtools.model.level2.process;
import org.biopax.paxtools.model.level2.transport;
import org.biopax.paxtools.model.level2.transportWithBiochemicalReaction;
import org.biopax.paxtools.proxy.level2.BioPAXElementProxy;
import org.biopax.paxtools.proxy.level2.biochemicalReactionProxy;
import org.biopax.paxtools.proxy.level2.catalysisProxy;
import org.biopax.paxtools.proxy.level2.complexAssemblyProxy;
import org.biopax.paxtools.proxy.level2.controlProxy;
import org.biopax.paxtools.proxy.level2.conversionProxy;
import org.biopax.paxtools.proxy.level2.interactionProxy;
import org.biopax.paxtools.proxy.level2.modulationProxy;
import org.biopax.paxtools.proxy.level2.physicalInteractionProxy;
import org.biopax.paxtools.proxy.level2.transportProxy;
import org.biopax.paxtools.proxy.level2.transportWithBiochemicalReactionProxy;

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
	 * get all interaction
	 * @return all interaction
	 */
	public Set<interaction> getInteractionList() {
		return getAllOneClassList(interaction.class, interactionProxy.class);
	}

	/**
	 * get all control
	 * @return all control
	 */
	public Set<control> getControlList() {
		return getAllOneClassList(control.class, controlProxy.class);
	}

	/**
	 * get all catalysis
	 * @return all catalysis
	 */
	public Set<catalysis> getCatalysisList() {
		return getAllOneClassList(catalysis.class, catalysisProxy.class);
	}

	/**
	 * get all modulation
	 * @return all modulation
	 */
	public Set<modulation> getModulationList() {
		return getAllOneClassList(modulation.class, modulationProxy.class);
	}

	/**
	 * get all conversion
	 * @return all conversion
	 */
	public Set<conversion> getConversionList() {
		return getAllOneClassList(conversion.class, conversionProxy.class);
	}

	/**
	 * get all biochemicalReaction
	 * @return all biochemicalReaction
	 */
	public Set<biochemicalReaction> getBiochemicalReactionList() {
		return getAllOneClassList(biochemicalReaction.class, biochemicalReactionProxy.class);
	}

	/**
	 * get all transportWithBiochemicalReaction
	 * @return all transportWithBiochemicalReaction
	 */
	public Set<transportWithBiochemicalReaction> getTransportWithBiochemicalReactionList() {
		return getAllOneClassList(transportWithBiochemicalReaction.class, transportWithBiochemicalReactionProxy.class);
	}

	/**
	 * get all complexAssembly
	 * @return all complexAssembly
	 */
	public Set<complexAssembly> getComplexAssemblyList() {
		return getAllOneClassList(complexAssembly.class, complexAssemblyProxy.class);
	}

	/**
	 * get all transport
	 * @return all transport
	 */
	public Set<transport> getTransportList() {
		return getAllOneClassList(transport.class, transportProxy.class);
	}

	/**
	 * get interaction by NAME
	 * @param regex
	 * @param bIncludeSynonyms
	 * @return interaction
	 */
	public Set<interaction> getInteractionListByNAME(String regex, boolean bIncludeSynonyms) {
		return getOneClassByNAME(interaction.class, regex, bIncludeSynonyms);
	}

	/**
	 * get interaction by INTERACTION_TYPE
	 * @param regex
	 * @return interaction
	 */
	public Set<interaction> getInteractionListByINTERACTION_TYPE(String regex) {
		Set result = new HashSet();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		result = fetchPhysicalInteractionByINTERACTION_TYPE(em, regex);
		return result;
	}

	protected Set<physicalInteraction> fetchPhysicalInteractionByINTERACTION_TYPE(EntityManager em, String regex) {
		// INTERACTION_TYPEを持つのはinteractionでなくphysicalInteraction
		// 2007.08.01 Takeshi Yoneki
		Set result = new HashSet();
		KeywordSearch ks = session.createKeywordSearch();
		List<BioPAXElement> ocvs = ks.fetch(em, BioPAXElementProxy.SEARCH_FIELD_TERM, regex, openControlledVocabulary.class);
		for (BioPAXElement ocv: ocvs) {
			List pis = em.createQuery(
				"select o from " + physicalInteractionProxy.class.getName() + " as o join o.INTERACTION_TYPE as p where p.RDFId = '" + 
					ocv.getRDFId() + "'").getResultList();
			if (pis != null)
				result.addAll(pis);
		}
		return result;
	}

	/**
	 * get biochemicalReaction by EC_NUMBER
	 * @param regex
	 * @return biochemicalReaction
	 */
	public Set<biochemicalReaction> getBiochemicalReactionListByEC_NUMBER(String regex) {
		Set result = new HashSet();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		KeywordSearch ks = session.createKeywordSearch();
		List<BioPAXElement> brList = ks.fetch(em, BioPAXElementProxy.SEARCH_FIELD_EC_NUMBER, regex, biochemicalReaction.class);
		result.addAll(brList);
		return result;
	}

	/**
	 * get control by CONTROLLED
	 * @param regex
	 * @param bIncludeSynonyms
	 * @return control
	 */
	public Set<control> getControlListByCONTROLLED(String regex, boolean bIncludeSynonyms) {
		Set<control> result = new HashSet<control>();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		result = fetchControlByCONTROLLED(em, regex, bIncludeSynonyms);
		return result;
	}

	protected Set<control> fetchControlByCONTROLLED(EntityManager em, String regex, boolean bIncludeSynonyms) {
		Set result = new HashSet();
		Set<process> ps = fetchOneClassByNAME(em, process.class, regex, bIncludeSynonyms);
		for (process pr: ps) {
			List cs = em.createQuery(
				"select o from " + controlProxy.class.getName() + " as o join o.CONTROLLED as p where p.RDFId = '" + 
					pr.getRDFId() + "'").getResultList();
			if (cs != null)
				result.addAll(cs);
		}
		return result;
	}

	/**
	 * get CONTROLLER By CONTROLLED
	 * @param regex
	 * @param bIncludeSynonyms
	 * @return physicalEntityParticipant
	 */
	public Set<physicalEntityParticipant> getCONTROLLERListByCONTROLLED(String regex, boolean bIncludeSynonyms) {
		Set<physicalEntityParticipant> result = new HashSet<physicalEntityParticipant>();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		Set<control> cs = fetchControlByCONTROLLED(em, regex, bIncludeSynonyms);
		for (control c: cs) {
			result.addAll(c.getCONTROLLER());
		}
		return result;
	}

}

