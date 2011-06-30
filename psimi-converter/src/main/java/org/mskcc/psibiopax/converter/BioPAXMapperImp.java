// $Id: BioPAXMapperImp.java,v 1.1 2009/11/22 15:50:28 rodche Exp $
//------------------------------------------------------------------------------
/** Copyright (c) 2009 Memorial Sloan-Kettering Cancer Center.
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/
package org.mskcc.psibiopax.converter;

// imports

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.*;
import org.biopax.paxtools.model.level3.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An base class that creates paxtool models given psi.
 *
 * @author Benjamin Gross
 */
public class BioPAXMapperImp implements BioPAXMapper
{

	/**
	 * Ref to biopax model.
	 */
	private Model bpModel;

	/**
	 * Ref to biopax level.
	 */
	private BioPAXLevel bpLevel;

	/**
	 * Set of open/controlled vocabulary.
	 */
	Set<openControlledVocabulary> vocabularyL2;
	Set<ControlledVocabulary> vocabularyL3;

	/**
	 * Constructor.
	 *
	 * @param bpLevel BioPAXLevel
	 */
	public BioPAXMapperImp(BioPAXLevel bpLevel)
	{

		// init members
		this.bpLevel = bpLevel;
		if (bpLevel == BioPAXLevel.L2)
		{
			bpModel = BioPAXLevel.L2.getDefaultFactory().createModel();
		}
		else if (bpLevel == BioPAXLevel.L3)
		{
			bpModel = BioPAXLevel.L3.getDefaultFactory().createModel();
		}
		this.vocabularyL2 = new HashSet<openControlledVocabulary>();
		this.vocabularyL3 = new HashSet<ControlledVocabulary>();
	}

	/**
	 * Gets the biopax model.
	 *
	 * @return Model
	 */
	public Model getModel()
	{
		return bpModel;
	}

	/**
	 * Gets a unification xref.
	 *
	 * @param id String
	 * @return <T extends BioPAXElement>
	 */
	public <T extends BioPAXElement> T getUnificationXref(String id)
	{

		if (bpLevel == BioPAXLevel.L2)
		{
			return (T) bpModel.addNew(unificationXref.class, id);
		}
		else if (bpLevel == BioPAXLevel.L3)
		{
			return (T) bpModel.addNew(UnificationXref.class, id);
		}

		// should not get here
		return null;
	}

	/**
	 * Gets a relationship xref.
	 *
	 * @param id        String
	 * @param refType   String
	 * @param refTypeID String
	 * @return <T extends BioPAXElement>
	 */
	public <T extends BioPAXElement> T getRelationshipXref(String id, String refType,
	                                                       String refTypeID)
	{

		if (bpLevel == BioPAXLevel.L2)
		{
			relationshipXref toReturn = bpModel.addNew(relationshipXref.class, id);
			if (refType != null)
			{
				toReturn.setRELATIONSHIP_TYPE(refType);
			}
			return (T) toReturn;
		}
		else if (bpLevel == BioPAXLevel.L3)
		{
			RelationshipXref toReturn = bpModel.addNew(RelationshipXref.class, id);
			if (refType != null && refTypeID != null)
			{
				RelationshipTypeVocabulary rtv =
						bpModel.addNew(RelationshipTypeVocabulary.class, refTypeID);
				rtv.addTerm(refType);
				toReturn.setRelationshipType(rtv);
			}
			return (T) toReturn;
		}

		// should not get here
		return null;
	}

	/**
	 * Gets a publication xref.
	 *
	 * @param id String
	 * @return <T extends BioPAXElement>
	 */
	public <T extends BioPAXElement> T getPublicationXref(String id)
	{

		if (bpLevel == BioPAXLevel.L2)
		{
			return (T) bpModel.addNew(publicationXref.class, id);
		}
		else if (bpLevel == BioPAXLevel.L3)
		{
			return (T) bpModel.addNew(PublicationXref.class, id);
		}

		// should not get here
		return null;
	}

	/**
	 * Gets an evidence object.
	 *
	 * @param id                  String
	 * @param bpXrefs             Set<? extends BioPAXElement>
	 * @param evidenceCodes       Set<? extends BioPAXElement>
	 * @param scoresOrConfidences Set<? extends BioPAXElement>
	 * @param comments            Set<String>
	 * @param experimentalForms   Set<? extends BioPAXElement>
	 * @return <T extends BioPAXElement>
	 */
	public <T extends BioPAXElement> T getEvidence(String id,
	                                               Set<? extends BioPAXElement> bpXrefs,
	                                               Set<? extends BioPAXElement> evidenceCodes,
	                                               Set<? extends BioPAXElement> scoresOrConfidences,
	                                               Set<String> comments,
	                                               Set<? extends BioPAXElement> experimentalForms)
	{
		if (bpLevel == BioPAXLevel.L2)
		{
			evidence bpEvidence = bpModel.addNew(evidence.class, id);
			if (bpXrefs != null && bpXrefs.size() > 0)
			{
				bpEvidence.setXREF((Set<xref>) bpXrefs);
			}
			if (evidenceCodes != null && evidenceCodes.size() > 0)
			{
				bpEvidence.setEVIDENCE_CODE((Set<openControlledVocabulary>) evidenceCodes);
			}
			if (scoresOrConfidences != null && scoresOrConfidences.size() > 0)
			{
				bpEvidence.setCONFIDENCE((Set<confidence>) scoresOrConfidences);
			}
			if (comments != null && comments.size() > 0)
			{
				bpEvidence.setCOMMENT(comments);
			}
			if (experimentalForms != null && experimentalForms.size() > 0)
			{
				bpEvidence.setEXPERIMENTAL_FORM((Set<experimentalForm>) experimentalForms);
			}
			return (T) bpEvidence;
		}
		else if (bpLevel == BioPAXLevel.L3)
		{
			Evidence bpEvidence = bpModel.addNew(Evidence.class, id);
			if (bpXrefs != null)
			{
				for (BioPAXElement bpXref : bpXrefs)
				{
					bpEvidence.addXref((Xref) bpXref);

				}
			}
			if (evidenceCodes != null && evidenceCodes.size() > 0)
			{
				// go through these hoops to keep EntryMapper from juggling different L3 ControlledVocabulary types
				Set<EvidenceCodeVocabulary> evidenceCodeVocabularies =
						new HashSet<EvidenceCodeVocabulary>();
				for (ControlledVocabulary cv : (Set<ControlledVocabulary>) evidenceCodes)
				{
					if (bpModel.contains(cv))
					{
						bpModel.remove(cv);
					}
					EvidenceCodeVocabulary ecv =
							bpModel.addNew(EvidenceCodeVocabulary.class, cv.getRDFId());
					replaceControlledVocabulary(cv, ecv);
					evidenceCodeVocabularies.add(ecv);
				}
			}
			if (scoresOrConfidences != null && scoresOrConfidences.size() > 0)
			{
				for (BioPAXElement score : scoresOrConfidences) {
					bpEvidence.addConfidence((Score)score);
				}
			}
			if (comments != null && comments.size() > 0)
			{
				for (String comment : comments) {
					bpEvidence.addComment(comment);
				}
			}
			if (experimentalForms != null && experimentalForms.size() > 0)
			{
				for (BioPAXElement experimentalForm : experimentalForms) {
					bpEvidence.addExperimentalForm((ExperimentalForm)experimentalForm);
				}
			}
			return (T) bpEvidence;
		}

		// should not get here
		return null;
	}

	/**
	 * Gets a confidence/score object.
	 *
	 * @param id       String
	 * @param value    String
	 * @param bpXrefs  Set<? extends BioPAXElement>
	 * @param comments Set<String>
	 * @return <T extends BioPAXElement>
	 */
	public <T extends BioPAXElement> T getScoreOrConfidence(String id,
	                                                        String value,
	                                                        Set<? extends BioPAXElement> bpXrefs,
	                                                        Set<String> comments)
	{

		if (bpLevel == BioPAXLevel.L2)
		{
			confidence bpConfidence = bpModel.addNew(confidence.class, id);
			if (value != null)
			{
				bpConfidence.setCONFIDENCE_VALUE(value);
			}
			if (bpXrefs != null && bpXrefs.size() > 0)
			{
				bpConfidence.setXREF((Set<xref>) bpXrefs);
			}
			if (comments != null && comments.size() > 0)
			{
				bpConfidence.setCOMMENT(comments);
			}
			return (T) bpConfidence;
		}
		else if (bpLevel == BioPAXLevel.L3)
		{
			Score bpScore = bpModel.addNew(Score.class, id);
			if (value != null)
			{
				bpScore.setValue(value);
			}
			if (bpXrefs != null && bpXrefs.size() > 0)
			{
				for (BioPAXElement xref : bpXrefs) {
					bpScore.addXref((Xref)xref);
				}
			}
			if (comments != null && comments.size() > 0)
			{
				for (String comment : comments) {
					bpScore.addComment(comment);
				}
			}
			return (T) bpScore;
		}

		// should not get here
		return null;
	}

	/**
	 * Gets a experimental form object.
	 *
	 * @param id          String
	 * @param formType    BioPAXElement
	 * @param participant BioPAXElement
	 * @return <T extends BioPAXElement>
	 */
	public <T extends BioPAXElement> T getExperimentalForm(String id,
	                                                       BioPAXElement formType,
	                                                       BioPAXElement participant)
	{
		if (bpLevel == BioPAXLevel.L2)
		{
			experimentalForm bpExperimentalForm =
					bpModel.addNew(experimentalForm.class, id);
			if (formType != null)
			{
				bpExperimentalForm.addEXPERIMENTAL_FORM_TYPE((openControlledVocabulary) formType);
			}
			if (participant != null)
			{
				bpExperimentalForm.setPARTICIPANT((physicalEntityParticipant) participant);
			}
			return (T) bpExperimentalForm;
		}
		else if (bpLevel == BioPAXLevel.L3)
		{
			ExperimentalForm bpExperimentalForm =
					bpModel.addNew(ExperimentalForm.class, id);
			if (formType != null)
			{
				// go through these hoops to keep EntryMapper from juggling different L3 ControlledVocabulary types
				if (bpModel.contains(formType))
				{
					bpModel.remove(formType);
				}
				ExperimentalFormVocabulary efv =
						bpModel
								.addNew(ExperimentalFormVocabulary.class, formType.getRDFId());
				replaceControlledVocabulary((ControlledVocabulary) formType,
						efv);
				bpExperimentalForm.addExperimentalFormDescription(efv);
			}
			if (participant != null)
			{
				bpExperimentalForm.setExperimentalFormEntity((Entity) participant);
			}
			return (T) bpExperimentalForm;
		}

		// should not get here
		return null;
	}

	/**
	 * Gets an existing open/controlled vocabulary object.
	 *
	 * @param termToSearch String
	 * @return <T extends BioPAXElement>
	 */
	public <T extends BioPAXElement> T getOpenControlledVocabulary(String termToSearch)
	{

		if (bpLevel == BioPAXLevel.L2)
		{
			for (openControlledVocabulary ocv : vocabularyL2)
			{
				for (String term : ocv.getTERM())
				{
					if (term.equals(termToSearch))
					{
						return (T) ocv;
					}
				}
			}
		}
		else if (bpLevel == BioPAXLevel.L3)
		{
			for (ControlledVocabulary cv : vocabularyL3)
			{
				for (String term : cv.getTerm())
				{
					if (term.equals(termToSearch))
					{
						return (T) cv;
					}
				}
			}
		}

		// should not get here
		return null;
	}

	/**
	 * Gets a open/controlled vocabulary object.
	 *
	 * @param id      String
	 * @param term    String
	 * @param bpXrefs Set<? extends BioPAXElement>
	 * @return <T extends BioPAXElement>
	 */
	public <T extends BioPAXElement> T getOpenControlledVocabulary(String id, String term,
	                                                               Set<? extends BioPAXElement> bpXrefs)
	{

		if (bpLevel == BioPAXLevel.L2)
		{
			openControlledVocabulary toReturn =
					bpModel.addNew(openControlledVocabulary.class, id);
			if (term != null)
			{
				Set<String> terms = new HashSet<String>();
				terms.add(term);
				toReturn.setTERM(terms);
			}
			if (bpXrefs != null && bpXrefs.size() > 0)
			{
				toReturn.setXREF((Set<xref>) bpXrefs);
			}
			vocabularyL2.add(toReturn);
			return (T) toReturn;
		}
		else if (bpLevel == BioPAXLevel.L3)
		{
			ControlledVocabulary toReturn =
					bpModel.addNew(ControlledVocabulary.class, id);
			if (term != null)
			{
				toReturn.addTerm(term);
			}
			if (bpXrefs != null && bpXrefs.size() > 0)
			{
				for (BioPAXElement bpXref : bpXrefs)
				{
					toReturn.addXref((Xref)bpXref);
				}
			}
			vocabularyL3.add(toReturn);
			return (T) toReturn;
		}

		// should not get here
		return null;
	}

	/**
	 * Given an RDF ID, returns a matching model element
	 *
	 * @param rdfID
	 * @return BioPAXElement
	 */
	public BioPAXElement getBioPAXElement(String rdfID)
	{
		// does a key exist ?
		return bpModel.getByID(rdfID);
	}

	/**
	 * Gets an interaction.
	 *
	 * @param id           String
	 * @param name         String
	 * @param shortName    String
	 * @param availability Set<String>
	 * @param participants Set<? extends BioPAXElement>
	 * @param bpEvidence   Set<? extends BioPAXElement>
	 * @return <T extends BioPAXElement>
	 */
	public <T extends BioPAXElement> T getInteraction(String id,
	                                                  String name, String shortName,
	                                                  Set<String> availability,
	                                                  Set<? extends BioPAXElement> participants,
	                                                  Set<? extends BioPAXElement> bpEvidence)
	{

		if (bpLevel == BioPAXLevel.L2)
		{
			physicalInteraction toReturn =
					bpModel.addNew(physicalInteraction.class, id);
			if (name != null)
			{
				toReturn.setNAME(name);
			}
			if (shortName != null)
			{
				toReturn.setSHORT_NAME(shortName);
			}
			if (availability != null && availability.size() > 0)
			{
				toReturn.setAVAILABILITY(availability);
			}
			if (participants != null && participants.size() > 0)
			{
				toReturn.setPARTICIPANTS((Set<InteractionParticipant>) participants);
			}
			if (bpEvidence != null && bpEvidence.size() > 0)
			{
				toReturn.setEVIDENCE((Set<evidence>) bpEvidence);
			}
			return (T) toReturn;
		}
		else if (bpLevel == BioPAXLevel.L3)
		{
			MolecularInteraction toReturn =
					bpModel.addNew(MolecularInteraction.class, id);
			if (name != null)
			{
				toReturn.setStandardName(name);
			}
			if (shortName != null)
			{
				toReturn.setDisplayName(shortName);
			}
			if (availability != null && availability.size() > 0)
			{
				for (String availabilityStr : availability) {
					toReturn.addAvailability(availabilityStr);
				}
			}
			if (participants != null && participants.size() > 0)
			{
				for (BioPAXElement participant : participants) {
					toReturn.addParticipant((Entity)participant);
				}
			}
			if (bpEvidence != null && bpEvidence.size() > 0)
			{
				for (BioPAXElement evidence : bpEvidence) {
					toReturn.addEvidence((Evidence)evidence);
				}
			}
			return (T) toReturn;
		}

		// should not get here
		return null;
	}

	/**
	 * Gets a participant.
	 *
	 * @param id               String
	 * @param features         <? extends BioPAXElement>
	 * @param cellularLocation BioPAXElement
	 * @param bpPhysicalEntity BioPAXElement
	 * @return <T extends BioPAXElement>
	 */
	public <T extends BioPAXElement> T getParticipant(String id,
	                                                  Set<? extends BioPAXElement> features,
	                                                  BioPAXElement cellularLocation,
	                                                  BioPAXElement bpPhysicalEntity)
	{
		if (bpLevel == BioPAXLevel.L2)
		{
			sequenceParticipant toReturn =
					bpModel.addNew(sequenceParticipant.class, id);
			if (features != null && features.size() > 0)
			{
				toReturn.setSEQUENCE_FEATURE_LIST((Set<sequenceFeature>) features);
			}
			if (cellularLocation != null)
			{
				toReturn.setCELLULAR_LOCATION((openControlledVocabulary) cellularLocation);
			}
			if (bpPhysicalEntity != null)
			{
				toReturn.setPHYSICAL_ENTITY((physicalEntity) bpPhysicalEntity);
			}
			return (T) toReturn;
		}
		else if (bpLevel == BioPAXLevel.L3)
		{
			if (bpPhysicalEntity != null)
			{
				if (features != null && features.size() > 0)
				{
					for (BioPAXElement feature : features) {
						((PhysicalEntity) bpPhysicalEntity).addFeature((EntityFeature)feature);
					}
				}
				if (cellularLocation != null && bpPhysicalEntity != null)
				{
					// go through these hoops to keep EntryMapper from juggling different L3 ControlledVocabulary types
					if (bpModel.contains(cellularLocation))
					{
						bpModel.remove(cellularLocation);
					}
					CellularLocationVocabulary clv =
							bpModel
									.addNew(CellularLocationVocabulary.class,
											cellularLocation.getRDFId());
					replaceControlledVocabulary((ControlledVocabulary) cellularLocation,
							clv);
					((PhysicalEntity) bpPhysicalEntity).setCellularLocation(clv);
				}
			}
			return (T) bpPhysicalEntity;
		}

		// should not get here
		return null;
	}

	/**
	 * Gets a physical Entity.
	 *
	 * @param physicalEntityType String
	 * @param id                 String
	 * @param name               String
	 * @param shortName          String
	 * @param synonyms           Set<String>
	 * @param bpXrefs            Set<? extends BioPAXElement>
	 * @param bioSource          BioPAXElement
	 * @param sequence           String
	 * @return <T extends BioPAXElement>
	 * @parma entityRefId String
	 */
	public <T extends BioPAXElement> T getPhysicalEntity(String physicalEntityType, String id,
	                                                     String name, String shortName,
	                                                     Set<String> synonyms,
	                                                     Set<? extends BioPAXElement> bpXrefs,
	                                                     String entityRefId,
	                                                     BioPAXElement bioSource,
	                                                     String sequence)
	{

		if (bpLevel == BioPAXLevel.L2)
		{
			physicalEntity toReturn = null;
			if (physicalEntityType != null && physicalEntityType.equalsIgnoreCase("small molecule"))
			{
				toReturn = bpModel.addNew(smallMolecule.class, id);
			}
			else if (physicalEntityType != null && physicalEntityType.equalsIgnoreCase("dna"))
			{
				toReturn = bpModel.addNew(dna.class, id);
			}
			else if (physicalEntityType != null && physicalEntityType.equalsIgnoreCase("rna"))
			{
				toReturn = bpModel.addNew(rna.class, id);
			}
			else
			{
				// default to protein
				toReturn = bpModel.addNew(protein.class, id);
				if (bioSource != null)
				{
					((protein) toReturn).setORGANISM((bioSource) bioSource);
				}
				if (sequence != null)
				{
					((protein) toReturn).setSEQUENCE(sequence);
				}
			}
			if (name != null)
			{
				toReturn.setNAME(name);
			}
			if (shortName != null)
			{
				toReturn.setSHORT_NAME(shortName);
			}
			for (String synonym : synonyms)
			{
				toReturn.addSYNONYMS(synonym);
			}
			if (bpXrefs != null && bpXrefs.size() > 0)
			{
				toReturn.setXREF((Set<xref>) bpXrefs);
			}

			return (T) toReturn;
		}
		else if (bpLevel == BioPAXLevel.L3)
		{
			SimplePhysicalEntity toReturn = null;
			EntityReference er = null;
			if (physicalEntityType != null && physicalEntityType.equalsIgnoreCase("small molecule"))
			{
				toReturn = bpModel.addNew(SmallMolecule.class, id);
				er = bpModel.addNew(SmallMoleculeReference.class, entityRefId);
			}
			else if (physicalEntityType != null && physicalEntityType.equalsIgnoreCase("dna"))
			{
				toReturn = bpModel.addNew(Dna.class, id);
				er = bpModel.addNew(DnaReference.class, entityRefId);
			}
			else if (physicalEntityType != null && physicalEntityType.equalsIgnoreCase("rna"))
			{
				toReturn = bpModel.addNew(Rna.class, id);
				er = bpModel.addNew(RnaReference.class, entityRefId);
			}
			else
			{
				// default to protein
				toReturn = bpModel.addNew(Protein.class, id);
				er = bpModel.addNew(ProteinReference.class, entityRefId);
			}
			if (name != null)
			{
				toReturn.setStandardName(name);
			}
			if (shortName != null)
			{
				toReturn.setDisplayName(shortName);
			}
			if (synonyms != null && synonyms.size() > 0)
			{
				for (String synonym : synonyms) {
					toReturn.addName(synonym);
				}
			}
			if (bpXrefs != null && bpXrefs.size() > 0)
			{
				for (BioPAXElement xref : bpXrefs) {
					toReturn.addXref((Xref)xref);
				}
			}
			// set sequence entity ref props

			if (er instanceof SequenceEntityReference) {
				SequenceEntityReference ser = (SequenceEntityReference)er;
				ser.setOrganism((BioSource) bioSource);
				ser.setSequence(sequence);
			}
			// set entity ref on pe
			toReturn.setEntityReference(er);

			return (T) toReturn;
		}

		// should not get here
		return null;
	}

	/**
	 * Gets a biosource.
	 *
	 * @param id        String
	 * @param taxonXref BioPAXElement
	 * @param cellType  BioPAXElement
	 * @param tissue    BioPAXElement
	 * @param name      String
	 * @return <T extends BioPAXElement>
	 */
	public <T extends BioPAXElement> T getBioSource(String id, BioPAXElement taxonXref,
	                                                BioPAXElement cellType, BioPAXElement tissue,
	                                                String name)
	{

		if (bpLevel == BioPAXLevel.L2)
		{
			bioSource toReturn = bpModel.addNew(bioSource.class, id);
			if (taxonXref != null)
			{
				toReturn.setTAXON_XREF((unificationXref) taxonXref);
			}
			if (cellType != null)
			{
				toReturn.setCELLTYPE((openControlledVocabulary) cellType);
			}
			if (tissue != null)
			{
				toReturn.setTISSUE((openControlledVocabulary) tissue);
			}
			if (name != null)
			{
				toReturn.setNAME(name);
			}
			return (T) toReturn;
		}
		else if (bpLevel == BioPAXLevel.L3)
		{
			BioSource toReturn = bpModel.addNew(BioSource.class, id);
			if (taxonXref != null)
			{
				toReturn.addXref((Xref)taxonXref);
			}
			if (cellType != null)
			{
				toReturn.setCellType((CellVocabulary) cellType);
			}
			if (tissue != null)
			{
				toReturn.setTissue((TissueVocabulary) tissue);
			}
			if (name != null)
			{
				toReturn.setStandardName(name);
			}
			return (T) toReturn;
		}

		// should not get here
		return null;
	}

	/**
	 * Used to add feature attributes to given sequence or entity feature.
	 *
	 * @param bpFeature BioPAXElement
	 * @param bpXrefs           Set<? extends BioPAXElement>
	 * @param featureLocations  Set<? extends BioPAXElement>
	 * @param featureType       BioPAXElement
	 * @return <T extends BioPAXElement>
	 */
	public <T extends BioPAXElement> T getFeature(BioPAXElement bpFeature,
	                                              Set<? extends BioPAXElement> bpXrefs,
	                                              Set<? extends BioPAXElement> featureLocations,
	                                              BioPAXElement featureType)
	{

		if (bpLevel == BioPAXLevel.L2)
		{
			sequenceFeature toReturn = (sequenceFeature) bpFeature;
			if (bpXrefs != null && bpXrefs.size() > 0)
			{
				for (xref bpXref : (Set<xref>) bpXrefs)
				{
					toReturn.addXREF(bpXref);
				}
			}
			if (featureLocations != null)
			{
				toReturn.setFEATURE_LOCATION((Set<sequenceLocation>) featureLocations);
			}
			if (featureType != null)
			{
				toReturn.setFEATURE_TYPE((openControlledVocabulary) featureType);
			}
			return (T) toReturn;

		}
		else if (bpLevel == BioPAXLevel.L3)
		{
			EntityFeature toReturn = (EntityFeature) bpFeature;
			// question: EntityFeature does not implement XReferrable
			if (featureLocations != null)
			{
				for (BioPAXElement sequenceLocation : featureLocations) {
					toReturn.setFeatureLocation((SequenceLocation) sequenceLocation);
				}
			}
			if (featureType != null)
			{
				toReturn.setFeatureLocationType((SequenceRegionVocabulary) featureType);
			}
			return (T) toReturn;
		}

		// should not get here
		return null;
	}

	/**
	 * Gets a sequence or entity feature.
	 *
	 * @param id               String
	 * @param bpXrefs          Set<? extends BioPAXElement>
	 * @param featureLocations Set<? extends BioPAXElement>
	 * @param featureType      BioPAXElement
	 * @return <T extends BioPAXElement>
	 */
	public <T extends BioPAXElement> T getFeature(String id,
	                                              Set<? extends BioPAXElement> bpXrefs,
	                                              Set<? extends BioPAXElement> featureLocations,
	                                              BioPAXElement featureType)
	{
		if (bpLevel == BioPAXLevel.L2)
		{
			sequenceFeature toReturn = bpModel.addNew(sequenceFeature.class, id);
			if (bpXrefs != null && bpXrefs.size() > 0)
			{
				toReturn.setXREF((Set<xref>) bpXrefs);
			}
			if (featureLocations != null)
			{
				toReturn.setFEATURE_LOCATION((Set<sequenceLocation>) featureLocations);
			}
			if (featureType != null)
			{
				toReturn.setFEATURE_TYPE((openControlledVocabulary) featureType);
			}
			return (T) toReturn;
		}
		else if (bpLevel == BioPAXLevel.L3)
		{
			EntityFeature toReturn = bpModel.addNew(EntityFeature.class, id);
			// question: EntityFeature does not implement XReferrable
			if (featureLocations != null)
			{
				for (BioPAXElement featureLocation : featureLocations) {
					toReturn.setFeatureLocation((SequenceLocation) featureLocation);
				}
			}
			if (featureType != null)
			{
				// go through these hoops to keep EntryMapper from juggling different L3 ControlledVocabulary types
				if (bpModel.contains(featureType))
				{
					bpModel.remove(featureType);
				}
				SequenceRegionVocabulary srv =
						bpModel
								.addNew(SequenceRegionVocabulary.class, featureType.getRDFId());
				replaceControlledVocabulary((ControlledVocabulary) featureType,
						srv);
				toReturn.setFeatureLocationType(srv);
			}
			return (T) toReturn;
		}

		// should not get here
		return null;
	}

	/**
	 * Gets a sequence location.
	 *
	 * @param seqLocationID         String
	 * @param beginSeqSiteID        String
	 * @param endSeqSiteID          String
	 * @param beginSequenceInterval long
	 * @param endSequenceInterval   long
	 * @return <T extends BioPAXElement>
	 */
	public <T extends BioPAXElement> T getSequenceLocation(String seqLocationID,
	                                                       String beginSeqSiteID,
	                                                       String endSeqSiteID,
	                                                       long beginSequenceInterval,
	                                                       long endSequenceInterval)
	{

		if (bpLevel == BioPAXLevel.L2)
		{
			sequenceInterval toReturn =
					bpModel.addNew(sequenceInterval.class, seqLocationID);
			sequenceSite bpSequenceSiteBegin =
					bpModel.addNew(sequenceSite.class, beginSeqSiteID);
			bpSequenceSiteBegin.setSEQUENCE_POSITION((int) beginSequenceInterval);
			toReturn.setSEQUENCE_INTERVAL_BEGIN(bpSequenceSiteBegin);
			sequenceSite bpSequenceSiteEnd =
					bpModel.addNew(sequenceSite.class, endSeqSiteID);
			bpSequenceSiteEnd.setSEQUENCE_POSITION((int) endSequenceInterval);
			toReturn.setSEQUENCE_INTERVAL_END(bpSequenceSiteEnd);
			return (T) toReturn;

		}
		else if (bpLevel == BioPAXLevel.L3)
		{
			SequenceInterval toReturn =
					bpModel.addNew(SequenceInterval.class, seqLocationID);
			SequenceSite bpSequenceSiteBegin =
					bpModel.addNew(SequenceSite.class, beginSeqSiteID);
			bpSequenceSiteBegin.setSequencePosition((int) beginSequenceInterval);
			toReturn.setSequenceIntervalBegin(bpSequenceSiteBegin);
			SequenceSite bpSequenceSiteEnd =
					bpModel.addNew(SequenceSite.class, endSeqSiteID);
			bpSequenceSiteEnd.setSequencePosition((int) endSequenceInterval);
			toReturn.setSequenceIntervalEnd(bpSequenceSiteEnd);
			return (T) toReturn;
		}

		// should not get here
		return null;
	}

	/**
	 * Given an xref (BioPAXElement) returns its id.
	 *
	 * @param bpXref BioPAXElement
	 * @return String
	 */
	public String getXrefID(BioPAXElement bpXref)
	{

		if (bpLevel == BioPAXLevel.L2)
		{
			return ((xref) bpXref).getID();
		}
		else if (bpLevel == BioPAXLevel.L3)
		{
			return ((Xref) bpXref).getId();
		}

		// should not get here
		return null;
	}

	/**
	 * Sets given xref's db and id.
	 *
	 * @param bpXref BioPAXElement
	 * @param db       String
	 * @param id       String
	 */
	public void setXrefDBAndID(BioPAXElement bpXref, String db, String id)
	{

		if (bpLevel == BioPAXLevel.L2)
		{
			((xref) bpXref).setDB(db);
			((xref) bpXref).setID(id);
		}
		else if (bpLevel == BioPAXLevel.L3)
		{
			((Xref) bpXref).setDb(db);
			((Xref) bpXref).setId(id);
		}
	}

	/**
	 * Sets biopax model namespace.
	 *
	 * @param namespace String
	 */
	public void setNamespace(String namespace)
	{
		bpModel.getNameSpacePrefixMap().put("", namespace);
	}

	/**
	 * Creates a data source on the model.
	 *
	 * @param id      String
	 * @param name    String
	 * @param bpXrefs Set<? extends BioPAXElement>
	 */
	public void setModelDataSource(String id, String name, Set<? extends BioPAXElement> bpXrefs)
	{
		setInteractionDataSource(null, id, name, bpXrefs);
	}

	/**
	 * Creates a data source and adds to given interaction.
	 *
	 * @param id      String
	 * @param bpXrefs Set<BioPAXElement>
	 * @parma interaction Object
	 */
	public <T extends BioPAXElement> void setInteractionDataSource(T interaction, String id,
	                                                               String name,
	                                                               Set<? extends BioPAXElement> bpXrefs)
	{

		if (bpLevel == BioPAXLevel.L2)
		{
			dataSource dSource = bpModel.addNew(dataSource.class, id);
			if (name != null)
			{
				dSource.addNAME(name);
			}
			if (bpXrefs != null && bpXrefs.size() > 0)
			{
				dSource.setXREF((Set<xref>) bpXrefs);
			}
			if (interaction != null)
			{
				((physicalInteraction) interaction).addDATA_SOURCE(dSource);
			}
		}
		else if (bpLevel == BioPAXLevel.L3)
		{
			Provenance provenance = bpModel.addNew(Provenance.class, id);
			if (name != null)
			{
				provenance.addName(name);
			}
			if (bpXrefs != null && bpXrefs.size() > 0)
			{
				for (BioPAXElement xref : bpXrefs) {
					provenance.addXref((Xref)xref);
				}
			}
			if (interaction != null)
			{
				((MolecularInteraction) interaction).addDataSource(provenance);
			}
		}
	}

	/**
	 * Given a set of evidence objects, determines if interaction (that evidence obj is derived from)
	 * is a genetic interaction.
	 *
	 * @param geneticInteractionTerms List<String>
	 * @param bpEvidence              Set<? extends BioPAXElement>
	 * @return boolean
	 */
	public boolean isGeneticInteraction(final List<String> geneticInteractionTerms,
	                                    Set<? extends BioPAXElement> bpEvidence)
	{

		if (bpLevel == BioPAXLevel.L2)
		{
			if (bpEvidence != null && bpEvidence.size() > 0)
			{
				for (evidence e : (Set<evidence>) bpEvidence)
				{
					Set<openControlledVocabulary> evidenceCodes = e.getEVIDENCE_CODE();
					if (evidenceCodes != null)
					{
						for (openControlledVocabulary ocv : evidenceCodes)
						{
							Set<String> terms = ocv.getTERM();
							if (terms != null)
							{
								for (String term : terms)
								{
									if (geneticInteractionTerms != null &&
									    geneticInteractionTerms.contains(term.toLowerCase()))
									{
										return true;
									}
								}
							}
						}
					}
				}
			}
		}
		else if (bpLevel == BioPAXLevel.L3)
		{
			if (bpEvidence != null && bpEvidence.size() > 0)
			{
				for (Evidence e : (Set<Evidence>) bpEvidence)
				{
					Set<EvidenceCodeVocabulary> evidenceCodes = e.getEvidenceCode();
					if (evidenceCodes != null)
					{
						for (EvidenceCodeVocabulary cv : evidenceCodes)
						{
							Set<String> terms = cv.getTerm();
							if (terms != null)
							{
								for (String term : terms)
								{
									if (geneticInteractionTerms != null &&
									    geneticInteractionTerms.contains(term.toLowerCase()))
									{
										return true;
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Replaces current cv with new one - only used in Level 3
	 *
	 * @param previous ControlledVocabulary
	 * @param current  ControlledVocababulary
	 */
	private void replaceControlledVocabulary(ControlledVocabulary previous,
	                                         ControlledVocabulary current)
	{
		for (String term : previous.getTerm()) {
			current.addTerm(term);
		}
		for (Xref xref : previous.getXref()) {
			current.addXref(xref);
		}
		if (vocabularyL3.contains(previous))
		{
			vocabularyL3.remove(previous);
		}
		vocabularyL3.add(current);
	}
}
