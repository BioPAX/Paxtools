/*
 * MiscSearch.java
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
			//"select o from pathwayStepProxy as o join o.NEXT_STEP as p where p.RDFId = '" + 
			"select o from " + resultProxyClass.getName() + " as o join o." + propName + " as p where p.RDFId = '" + 
				inElem.getRDFId() + "'").getResultList();
		if (s != null)
			result.addAll(s);
		return result;
	}

	Set getBackPointer(Class resultProxyClass, String propName, BioPAXElement inElem) {
		Set result = new HashSet();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		result = fetchBackPointer(em, resultProxyClass, propName, inElem);
		return result;
	}

	Set getBackPointerForMultiClass(Class[] resultProxyClassArray, String propName, BioPAXElement inElem) {
		Set result = new HashSet();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		for (Class c : resultProxyClassArray) {
			result.addAll(fetchBackPointer(em, c, propName, inElem));
		}
		return result;
	}

	///////////////////////////////
	// 逆ポインタ

	/**
	 * get back pointer of Participant
	 * @param Entity
	 * @return set of Interaction
	 */
	public Set<Interaction> isParticipantOf(Entity e) {
		return getBackPointer(InteractionProxy.class, "Participant", e);
	}

	/**
	 * get back pointer of Controller
	 * @param PhysicalEntity or Pathway
	 * @return set of Control
	 */
	// Pathwayを渡せるControl.ControllerはまだModelで規定されていない。
	// とりあえずPhysicalEntity or Pathwayは共通の継承元のEntityにしておく。
	// 2008.02.29 Takeshi Yoneki
	public Set<Control> isControllerOf(Entity e) {
		if (!(e instanceof PhysicalEntity) && !(e instanceof Pathway))
			return new HashSet();
		return getBackPointer(ControlProxy.class, "Controller", e);
	}

	/**
	 * get back pointer of Cofactor
	 * @param PhysicalEntity
	 * @return back pointer of Cofactor
	 */
	public Set<Catalysis> isCofactorOf(PhysicalEntity pe) {
		return getBackPointer(CatalysisProxy.class, "Cofactor", pe);
	}

	/**
	 * get back pointer of Controlled
	 * @param Process
	 * @return back pointer of Controlled
	 */
	public Set<Control> isControlledOf(Process p) {
		return getBackPointer(ControlProxy.class, "Controlled", p);
	}

	/**
	 * get back pointer of StepProcess
	 * @param Process
	 * @return back pointer of StepProcess
	 */
	public Set<PathwayStep> isStepProcessOf(Process p) {
		return getBackPointer(PathwayStepProxy.class, "StepProcess", p);
	}

	/**
	 * get back pointer of StepConversion
	 * @param Conversion
	 * @return back pointer of StepConversion
	 */
	public Set<BiochemicalPathwayStep> isStepConversionOf(Conversion c) {
		return getBackPointer(BiochemicalPathwayStepProxy.class, "StepConversion", c);
	}

	/**
	 * get back pointer of InteractionType
	 * @param ControlledVocabulary
	 * @return back pointer of InteractionType
	 */
	public Set<Interaction> isInteractionTypeOf(ControlledVocabulary cv) {
		return getBackPointer(BiochemicalPathwayStepProxy.class, "InteractionType", cv);
	}

	/**
	 * get back pointer of Confidence
	 * @param Score
	 * @return back pointer of Confidence
	 */
	public Set<Evidence> isConfidenceOf(Score sc) {
		return getBackPointer(EvidenceProxy.class, "Confidence", sc);
	}

	/**
	 * get back pointer of EvidenceCode
	 * @param EvidenceCodeVocabulary
	 * @return back pointer of EvidenceCode
	 */
	public Set<Evidence> isEvidenceCodeOf(EvidenceCodeVocabulary ecv) {
		return getBackPointer(EvidenceProxy.class, "EvidenceCode", ecv);
	}

	/**
	 * get back pointer of ExperimentalForm
	 * @param ExperimentalForm
	 * @return back pointer of ExperimentalForm
	 */
	public Set<Evidence> isExperimentalFormOf(ExperimentalForm ef) {
		return getBackPointer(EvidenceProxy.class, "ExperimentalForm", ef);
	}

	/**
	 * get back pointer of Feature
	 * @param EntityFeature
	 * @return back pointer of Feature
	 */
	public Set<PhysicalEntity> isFeatureOf(EntityFeature ef) {
		return getBackPointer(PhysicalEntityProxy.class, "Feature", ef);
	}

	/**
	 * get back pointer of NotFeature
	 * @param EntityFeature
	 * @return back pointer of NotFeature
	 */
	public Set<PhysicalEntity> isNotFeatureOf(EntityFeature ef) {
		return getBackPointer(PhysicalEntityProxy.class, "NotFeature", ef);
	}

	/**
	 * get back pointer of Xref
	 * @param Xref
	 * @return back pointer of Xref
	 */
	public Set<XReferrable> isXrefOf(Xref x) {
		return getBackPointerForMultiClass(
			new Class[] { 
				ControlledVocabularyProxy.class, 
				EntityProxy.class,
				EvidenceProxy.class,
				ProvenanceProxy.class,
				ScoreProxy.class }, 
			"Xref", x);
	}

	/**
	 * get back pointer of ExperimentalFormDescription
	 * @param ExperimentalFormVocabulary
	 * @return back pointer of ExperimentalFormDescription
	 */
	public Set<ExperimentalForm> isExperimentalFormDescriptionOf(ExperimentalFormVocabulary efv) {
		return getBackPointer(ExperimentalFormProxy.class, "ExperimentalFormDescription", efv);
	}

	/**
	 * get back pointer of ExperimentalFeature
	 * @param EntityFeature
	 * @return back pointer of ExperimentalFeature
	 */
	public Set<ExperimentalForm> isExperimentalFeatureOf(EntityFeature ef) {
		return getBackPointer(ExperimentalFormProxy.class, "EntityFeature", ef);
	}

	/**
	 * get back pointer of ComponentStoichiometry
	 * @param PhysicalEntity
	 * @return back pointer of ComponentStoichiometry
	 */
	// ComponentStoichiometryはまだModelで規定されていない。
	// 2008.02.29 Takeshi Yoneki
	public Set<Complex> isComponentStoichiometryOf(PhysicalEntity pe) {
		return getBackPointer(EntityProxy.class, "ComponentStoichiometry", pe);
	}

	/**
	 * get back pointer of FeatureLocation
	 * @param SequenceLocation
	 * @return back pointer of FeatureLocation
	 */
	public Set<EntityFeature> isFeatureLocationOf(SequenceLocation sl) {
		return getBackPointer(EntityFeatureProxy.class, "FeatureLocation", sl);
	}

	/**
	 * get back pointer of DataSource
	 * @param Provenance
	 * @return back pointer of DataSource
	 */
	public Set<Entity> isDataSourceOf(Provenance p) {
		return getBackPointer(EntityProxy.class, "DataSource", p);
	}

	/**
	 * get back pointer of RegulatoryElement
	 * @param Dna or Rna
	 * @return back pointer of RegulatoryElement
	 */
	// RegulatoryElementはまだModelで規定されていない。
	// とりあえずDna or Rnaは共通の継承元のPhysicalEntityにしておく。
	// 2008.02.29 Takeshi Yoneki
	public Set<TemplateReaction> isRegulatoryElementOf(PhysicalEntity pe) {
		if (!(pe instanceof Dna) && !(pe instanceof Rna))
			return new HashSet();
		return getBackPointer(TemplateReactionProxy.class, "RegulatoryElement", pe);
	}

	/**
	 * get back pointer of PathwayComponent
	 * @param Process
	 * @return back pointer of PathwayComponent
	 */
	public Set<Pathway> isPathwayComponentOf(Process p) {
		return getBackPointer(PathwayProxy.class, "PathwayComponent", p);
	}

	/**
	 * get back pointer of BoundTo
	 * @param BindingFeature
	 * @return back pointer of BoundTo
	 */
	public Set<BindingFeature> isBoundToOf(BindingFeature bf) {
		return getBackPointer(BindingFeatureProxy.class, "BoundTo", bf);
	}

	/**
	 * get back pointer of MemberEntity
	 * @param EntityReference
	 * @return back pointer of MemberEntity
	 */
	public Set<EntityReference> isMemberEntityOf(EntityReference er) {
		return getBackPointer(EntityReferenceProxy.class, "MemberEntity", er);
	}

	/**
	 * get back pointer of PathwayOrder
	 * @param PathwayStep
	 * @return back pointer of PathwayOrder
	 */
	public Set<Pathway> isPathwayOrderOf(PathwayStep ps) {
		return getBackPointer(PathwayProxy.class, "PathwayOrder", ps);
	}

	/**
	 * get back pointer of EntityFeature
	 * @param EntityFeature
	 * @return back pointer of EntityFeature
	 */
	public Set<EntityReference> isEntityFeatureOf(EntityFeature ef) {
		return getBackPointer(EntityReferenceProxy.class, "EntityFeature", ef);
	}

	/**
	 * get back pointer of Product
	 * @param Dna or Protein or Rna
	 * @return back pointer of Product
	 */
	// ProductはまだModelで規定されていない。
	// とりあえずDna or Protein or Rnaは共通の継承元のPhysicalEntityにしておく。
	// 2008.02.29 Takeshi Yoneki
	public Set<TemplateReaction> isProductOf(PhysicalEntity pe) {
		if (!(pe instanceof Dna) && !(pe instanceof Rna) && !(pe instanceof Protein))
			return new HashSet();
		return getBackPointer(TemplateReactionProxy.class, "Product", pe);
	}

	/**
	 * get back pointer of KEQ
	 * @param KPrime
	 * @return back pointer of KEQ
	 */
	public Set<BiochemicalReaction> isKEQOf(KPrime kp) {
		return getBackPointer(BiochemicalReactionProxy.class, "KEQ", kp);
	}

	/**
	 * get back pointer of GroupType
	 * @param EntityReferenceGroupVocabulary
	 * @return back pointer of GroupType
	 */
	public Set<EntityReference> isEntityReferenceTypeOf(EntityReferenceTypeVocabulary ergv) {
		return getBackPointer(EntityReferenceProxy.class, "EntityReferenceType", ergv);
	}

	/**
	 * get back pointer of DeltaGPrime0
	 * @param DeltaGPrime0
	 * @return back pointer of DeltaGPrime0
	 */
	public Set<BiochemicalReaction> isDeltaGOf(DeltaG dgp) {
		return getBackPointer(BiochemicalReactionProxy.class, "DeltaG", dgp);
	}

	/**
	 * get back pointer of InteractionScore
	 * @param Score
	 * @return back pointer of InteractionScore
	 */
	public Set<GeneticInteraction> isInteractionScoreOf(Score s) {
		return getBackPointer(GeneticInteractionProxy.class, "InteractionScore", s);
	}

	/**
	 * get back pointer of ExperimentalFormEntity
	 * @param PhysicalEntity or Gene
	 * @return set of ExperimentalForm
	 */
	// ExperimentalFormEntityはまだModelで規定されていない。
	// とりあえずPhysicalEntity or Geneは共通の継承元のEntityにしておく。
	// 2008.02.29 Takeshi Yoneki
	public Set<ExperimentalForm> isExperimentalFormEntityOf(Entity e) {
		if (!(e instanceof PhysicalEntity) && !(e instanceof Gene))
			return new HashSet();
		return getBackPointer(ExperimentalFormProxy.class, "ExperimentalFormEntity", e);
	}

	/**
	 * get back pointer of NextStep
	 * @param PathwayStep
	 * @return set of PathwayStep
	 */
	public Set<PathwayStep> isNextStepOf(PathwayStep ps) {
		return getBackPointer(PathwayStepProxy.class, "NextStep", ps);
	}

	/**
	 * get back pointer of MemberLocation
	 * @param SequenceLocation
	 * @return set of SequenceLocation
	 */
	public Set<SequenceLocation> isMemberLocationOf(SequenceLocation ml) {
		return getBackPointer(SequenceLocationProxy.class, "MemberLocation", ml);
	}

	/**
	 * get back pointer of ParticipantStoichiometry
	 * @param Stoichiometry
	 * @return set of Conversion
	 */
	public Set<Conversion> isParticipantStoichiometryOf(Stoichiometry s) {
		return getBackPointer(ConversionProxy.class, "ParticipantStoichiometry", s);
	}

	/**
	 * get back pointer of Evidence
	 * @param Evidence
	 * @return set of Observable
	 */
	public Set<Observable> isEvidenceOf(Evidence e) {
		return getBackPointerForMultiClass(
			new Class[] { 
				EntityFeatureProxy.class, 
				EntityProxy.class,
				EntityReferenceProxy.class,
				PathwayStepProxy.class,
				PhysicalEntityProxy.class,
				ProcessProxy.class }, 
			"Evidence", e);
	}

	/**
	 * get back pointer of BindsTo
	 * @param PhysicalEntity
	 * @return set of PhysicalEntity
	 */
	public Set<PhysicalEntity> isBindsToOf(PhysicalEntity pe) {
		return getBackPointer(PhysicalEntityProxy.class, "BindsTo", pe);
	}

	/**
	 * get back pointer of Template
	 * @param Dna or Rna
	 * @return set of TemplateReaction
	 */
	// TemplateはまだModelで規定されていない。
	// とりあえずDna or Rnaは共通の継承元のPhysicalEntityにしておく。
	// 2008.02.29 Takeshi Yoneki
	public Set<TemplateReaction> isTemplateOf(PhysicalEntity pe) {
		if (!(pe instanceof Dna) && !(pe instanceof Rna))
			return new HashSet();
		return getBackPointer(TemplateReactionProxy.class, "Template", pe);
	}

	/**
	 * get back pointer of Structure
	 * @param ChemicalStructure
	 * @return set of SmallMoleculeReference
	 */
	public Set<SmallMoleculeReference> isStructureOf(ChemicalStructure cs) {
		return getBackPointer(SmallMoleculeReferenceProxy.class, "Structure", cs);
	}

	/**
	 * get back pointer of PhysicalEntity
	 * @param PhysicalEntity
	 * @return set of Stoichiometry
	 */
	public Set<Stoichiometry> isPhysicalEntityOf(PhysicalEntity pe) {
		return getBackPointer(StoichiometryProxy.class, "PhysicalEntity", pe);
	}

	/**
	 * get back pointer of Tissue
	 * @param TissueVocabulary
	 * @return set of BioSource
	 */
	public Set<BioSource> isTissueOf(TissueVocabulary tv) {
		return getBackPointer(BioSourceProxy.class, "Tissue", tv);
	}

	/**
	 * get back pointer of SequenceIntervalBegin
	 * @param SequenceSite
	 * @return set of SequenceInterval
	 */
	public Set<SequenceInterval> isSequenceIntervalBeginOf(SequenceSite ss) {
		return getBackPointer(SequenceIntervalProxy.class, "SequenceIntervalBegin", ss);
	}

	/**
	 * get back pointer of Organism
	 * @param BioSource
	 * @return set of Gene or SequenceEntityReference
	 */
	// とりあえずGene or SequenceEntityReferenceは共通の継承元のBioPAXElementにしておく。
	// 2008.02.29 Takeshi Yoneki
	public Set<BioPAXElement> isOrganismOf(BioSource bs) {
		return getBackPointerForMultiClass(
			new Class[] { 
				GeneProxy.class, 
				SequenceEntityReferenceProxy.class }, 
			"Organism", bs);
	}

	/**
	 * get back pointer of CellType
	 * @param CellVocabulary
	 * @return set of BioSource
	 */
	public Set<BioSource> isCellTypeOf(CellVocabulary cv) {
		return getBackPointer(BioSourceProxy.class, "CellType", cv);
	}

	/**
	 * get back pointer of Phenotype
	 * @param PhenotypeVocabulary
	 * @return set of GeneticInteraction
	 */
	public Set<GeneticInteraction> isPhenotypeOf(PhenotypeVocabulary pv) {
		return getBackPointer(GeneticInteractionProxy.class, "Phenotype", pv);
	}

	/**
	 * get back pointer of CellularLocation
	 * @param CellularLocationVocabulary
	 * @return set of PhysicalEntity
	 */
	public Set<PhysicalEntity> isCellularLocationOf(CellularLocationVocabulary clv) {
		return getBackPointer(PhysicalEntityProxy.class, "CellularLocation", clv);
	}

	/**
	 * get back pointer of TaxonXref
	 * @param UnificationXref
	 * @return set of BioSource
	 */
	public Set<BioSource> isTaxonXrefOf(UnificationXref ux) {
		return getBackPointer(BioSourceProxy.class, "TaxonXref", ux);
	}

	/**
	 * get back pointer of FeatureType
	 * @param SequenceModificationVocabulary
	 * @return set of ModificationFeature
	 */
	public Set<ModificationFeature> isFeatureTypeOf(SequenceModificationVocabulary smv) {
		return getBackPointer(ModificationFeatureProxy.class, "FeatureType", smv);
	}

	/**
	 * get back pointer of EntityReference
	 * @param EntityReference
	 * @return set of PhysicalEntity
	 */
	public Set<PhysicalEntity> isEntityReferenceOf(EntityReference er) {
		return getBackPointer(PhysicalEntityProxy.class, "EntityReference", er);
	}

	/**
	 * get back pointer of SequenceIntervalEnd
	 * @param SequenceSite
	 * @return set of SequenceInterval
	 */
	public Set<SequenceInterval> isSequenceIntervalEndOf(SequenceSite ss) {
		return getBackPointer(SequenceIntervalProxy.class, "SequenceIntervalEnd", ss);
	}

	/**
	 * get back pointer of Component
	 * @param PhysicalEntity
	 * @return set of SequenceInterval
	 */
	public Set<Complex> isComponentOf(PhysicalEntity ss) {
		return getBackPointer(ComplexProxy.class, "Component", ss);
	}

	///////////////////////////////
	// その他

	/**
	 * get Entity by SourceName of DataSource
	 * @param regexDB
	 * @param id
	 * @return entities
	 */
	public Set<Entity> getEntityListByDataSource(String regex) {
		Set<Entity> result = new HashSet<Entity>();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		KeywordSearch ks = createKeywordSearch();
		HashSet<BioPAXElement> ps = new HashSet<BioPAXElement>();
		ps.addAll(ks.fetch(em, BioPAXElementProxy.SEARCH_FIELD_SOURCE_NAME, regex, Provenance.class));
		for (BioPAXElement p: ps) {
			List es = em.createQuery(
				"select o from " + EntityProxy.class.getName() + " as o join o.DataSource as p where p.RDFId = '" + 
					p.getRDFId() + "'").getResultList();
			if (es != null)
				result.addAll(es);
		}
		return result;
	}

	/**
	 * get Entity by DB and ID of Xref
	 * @param regexDB
	 * @param id
	 * @return entities
	 */
	public Set<Entity> getEntityListByDbAndIdOfXref(String regexDB, String id) {
		Set<Entity> result = new HashSet<Entity>();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		KeywordSearch ks = createKeywordSearch();
		HashSet<BioPAXElement> xs = new HashSet<BioPAXElement>();
		HashSet<BioPAXElement> xsdb = new HashSet<BioPAXElement>();
		HashSet<BioPAXElement> xsid = new HashSet<BioPAXElement>();
		xsdb.addAll(ks.fetch(em, BioPAXElementProxy.SEARCH_FIELD_XREF_DB, regexDB, Xref.class));
		xsid.addAll(ks.fetch(em, BioPAXElementProxy.SEARCH_FIELD_XREF_ID, id, Xref.class));
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
				"select o from " + EntityProxy.class.getName() + " as o join o.Xref as p where p.RDFId = '" + 
					x.getRDFId() + "'").getResultList();
			if (es != null)
				result.addAll(es);
		}
		return result;
	}

	/**
	 * get Entity by Name (level 3 is different)
	 * @param regex
	 * @return entity list
	 */
	public Set<Entity> getEntityListByName(String regex) {
		return getOneClassByName(Entity.class, regex);
	}

	/**
	 * get Provenance
	 * @return Provenance SourceName
	 */

	public Set<String> getProvenanceList() {
		HashSet<String> result = new HashSet<String>();
// SourceNameがなくなった。
// とりあえずコンパイルを通す。
// 未実装。
// 2008.06.06 Takeshi Yoneki
//		Set dss = getAllOneClassList(Provenance.class, ProvenanceProxy.class);
//		for (Object ds: dss) {
//			String name = ((Provenance)ds).getSourceName();
//			result.add(name);
//		}
		return result;
	}

	/**
	 * get entity by Avairability
	 * @param regex
	 * @return entity list
	 */
	public Set<Entity> getEntityListByAvairability(String regex) {
		Set result = new HashSet();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		KeywordSearch ks = createKeywordSearch();
		result.addAll(ks.fetch(em, BioPAXElementProxy.SEARCH_FIELD_AVAILABILITY, regex, Entity.class));
		return result;
	}

	/**
	 * get entity by COMMENT
	 * @param regex
	 * @return entities
	 */
	public Set<Entity> getEntityListByComment(String regex) {
		Set result = new HashSet();
		if (session.setup() == false)
			return result;
		EntityManager em = session.getEntityManager();
		KeywordSearch ks = createKeywordSearch();
		result.addAll(ks.fetch(em, BioPAXElementProxy.SEARCH_FIELD_COMMENT, regex, Entity.class));
		return result;
	}

	/**
	 * get Title
	 * @return Title
	 */
	public Set<String> getTitleList() {
		HashSet<String> result = new HashSet<String>();
		Set dss = getAllOneClassList(PublicationXref.class, PublicationXrefProxy.class);
		for (Object ds: dss) {
			String name = ((PublicationXref)ds).getTitle();
			result.add(name);
		}
		return result;
	}

	/**
	 * get Tissue
	 * @return Tissue
	 */
	public Set<String> getTissueList() {
		HashSet<String> result = new HashSet<String>();
		Set dss = getAllOneClassList(BioSource.class, BioSourceProxy.class);
		for (Object ds: dss) {
			TissueVocabulary tv = ((BioSource)ds).getTissue();
			result.addAll(tv.getTerm());
		}
		return result;
	}
}

