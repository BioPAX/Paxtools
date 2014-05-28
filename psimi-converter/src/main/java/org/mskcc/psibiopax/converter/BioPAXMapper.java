// $Id: BioPAXMapper.java,v 1.1 2009/11/22 15:50:28 rodche Exp $
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
import java.util.Random;
import java.util.Set;

/**
 * An base class that creates paxtool models given psi.
 *
 * @author Benjamin Gross; rodche (refactored: URI generator, xmlBase, etc.)
 */
class BioPAXMapper {
	
	private Model bpModel;

	private BioPAXLevel bpLevel;
	
	private final String xmlBase;
	
	private final Random random;

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
	public BioPAXMapper(BioPAXLevel bpLevel, String xmlBase){
		
		this.random = new Random(System.currentTimeMillis());
// the setSeed below always throws an UOE (it's overloaded, no need to call)
//		this.threadLocalRandom.setSeed(System.currentTimeMillis());
		
		this.bpLevel = bpLevel;
		this.xmlBase = xmlBase;
		
		if (bpLevel == BioPAXLevel.L2)
		{
			bpModel = BioPAXLevel.L2.getDefaultFactory().createModel();
		}
		else if (bpLevel == BioPAXLevel.L3)
		{
			bpModel = BioPAXLevel.L3.getDefaultFactory().createModel();
		}
		
		this.bpModel.setXmlBase(this.xmlBase);
		
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
	 * New UnificationXref (Level3) or unificationXref (Level2).
	 * @param uri
	 * @return
	 */
	public <T extends BioPAXElement> T addNewUnificationXref(String uri) {
		if(uri == null) uri = genUri(UnificationXref.class);//works for L2 and L3, no problem here
		return (bpLevel == BioPAXLevel.L3) 
			? (T) bpModel.addNew(UnificationXref.class, uri) 
				: (T) bpModel.addNew(unificationXref.class, uri);
	}

	/**
	 * Gets a relationship xref.
	 *
	 * @param uri        String
	 * @param refType   String
	 * @return
	 */
	public <T extends BioPAXElement> T getRelationshipXref(String uri, String refType)
	{

		if (bpLevel == BioPAXLevel.L2)
		{
			relationshipXref toReturn = bpModel.addNew(relationshipXref.class, uri);
			if (refType != null)
			{
				toReturn.setRELATIONSHIP_TYPE(refType);
			}
			return (T) toReturn;
		}
		else if (bpLevel == BioPAXLevel.L3)
		{
			RelationshipXref toReturn = bpModel.addNew(RelationshipXref.class, uri);
			if (refType != null)
			{
				RelationshipTypeVocabulary rtv = bpModel
					.addNew(RelationshipTypeVocabulary.class, genUri(RelationshipTypeVocabulary.class));
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
	 * @param bpXrefs             Set<? extends BioPAXElement>
	 * @param evidenceCodes       Set<? extends BioPAXElement>
	 * @param scoresOrConfidences Set<? extends BioPAXElement>
	 * @param comments            Set<String>
	 * @param experimentalForms   Set<? extends BioPAXElement>
	 * @return
	 */
	public <T extends BioPAXElement> T getEvidence(Set<? extends BioPAXElement> bpXrefs,
	                                               Set<? extends BioPAXElement> evidenceCodes,
	                                               Set<? extends BioPAXElement> scoresOrConfidences,
	                                               Set<String> comments,
	                                               Set<? extends BioPAXElement> experimentalForms)
	{
		if (bpLevel == BioPAXLevel.L2)
		{
			evidence bpEvidence = bpModel.addNew(evidence.class, genUri(evidence.class));
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
			Evidence bpEvidence = bpModel.addNew(Evidence.class, genUri(Evidence.class));
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
				for (ControlledVocabulary cv : (Set<ControlledVocabulary>) evidenceCodes)
				{
					if (bpModel.contains(cv))
					{
						bpModel.remove(cv);
					}
					EvidenceCodeVocabulary ecv =
							bpModel.addNew(EvidenceCodeVocabulary.class, cv.getRDFId());
					replaceControlledVocabulary(cv, ecv);
					
					if(!bpEvidence.getEvidenceCode().contains(ecv))
						bpEvidence.addEvidenceCode(ecv);
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
	 * @param value    String
	 * @param bpXrefs  Set<? extends BioPAXElement>
	 * @param comments Set<String>
	 * @return
	 */
	public <T extends BioPAXElement> T getScoreOrConfidence(String value,
	                                                        Set<? extends BioPAXElement> bpXrefs,
	                                                        Set<String> comments)
	{

		if (bpLevel == BioPAXLevel.L2)
		{
			confidence bpConfidence = bpModel.addNew(confidence.class, genUri(confidence.class));
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
			Score bpScore = bpModel.addNew(Score.class, genUri(Score.class));
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
	 * @param formType    BioPAXElement
	 * @param participant BioPAXElement
	 * @return
	 */
	public <T extends BioPAXElement> T getExperimentalForm(BioPAXElement formType,
	                                                       BioPAXElement participant)
	{
		if (bpLevel == BioPAXLevel.L2)
		{
			experimentalForm bpExperimentalForm =
					bpModel.addNew(experimentalForm.class, genUri(experimentalForm.class));
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
					bpModel.addNew(ExperimentalForm.class, genUri(ExperimentalForm.class));
			if (formType != null)
			{
				// go through these hoops to keep EntryMapper from juggling different L3 ControlledVocabulary types
				if (bpModel.contains(formType))
				{
					bpModel.remove(formType);
				}
				ExperimentalFormVocabulary efv =
						bpModel.addNew(ExperimentalFormVocabulary.class, formType.getRDFId());
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
	 * @param term    String
	 * @param bpXrefs Set<? extends BioPAXElement>
	 * @return <T extends BioPAXElement>
	 */
	public <T extends BioPAXElement> T getOpenControlledVocabulary(String term,
	                                                               Set<? extends BioPAXElement> bpXrefs)
	{

		if (bpLevel == BioPAXLevel.L2)
		{
			openControlledVocabulary toReturn = bpModel
				.addNew(openControlledVocabulary.class, genUri(openControlledVocabulary.class));
			if (term != null) {
				Set<String> terms = new HashSet<String>();
				terms.add(term);
				toReturn.setTERM(terms);
			}

			if (bpXrefs != null && bpXrefs.size() > 0)
				toReturn.setXREF((Set<xref>) bpXrefs);

			vocabularyL2.add(toReturn);
			
			return (T) toReturn;
		}
		else if (bpLevel == BioPAXLevel.L3)
		{
			ControlledVocabulary toReturn = bpModel
				.addNew(ControlledVocabulary.class, genUri(ControlledVocabulary.class));
			
			if (term != null)
				toReturn.addTerm(term);

			if (bpXrefs != null && bpXrefs.size() > 0)
				for (BioPAXElement bpXref : bpXrefs)
					toReturn.addXref((Xref)bpXref);

			vocabularyL3.add(toReturn);
			
			return (T) toReturn;
		}

		// should not get here
		return null;
	}


	/**
	 * Gets an interaction.
	 *
	 * @param name         String
	 * @param shortName    String
	 * @param availability Set<String>
	 * @param participants Set<? extends BioPAXElement>
	 * @param bpEvidence   Set<? extends BioPAXElement>
	 * @return <T extends BioPAXElement>
	 */
	public <T extends BioPAXElement> T getInteraction(String name, String shortName,
	                                                  Set<String> availability,
	                                                  Set<? extends BioPAXElement> participants,
	                                                  Set<? extends BioPAXElement> bpEvidence)
	{

		if (bpLevel == BioPAXLevel.L2)
		{
			physicalInteraction toReturn =
					bpModel.addNew(physicalInteraction.class, genUri(physicalInteraction.class));
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
					bpModel.addNew(MolecularInteraction.class, genUri(MolecularInteraction.class));
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
	 * @param features         <? extends BioPAXElement>
	 * @param cellularLocation BioPAXElement
	 * @param bpPhysicalEntity BioPAXElement
	 * @return <T extends BioPAXElement>
	 */
	public <T extends BioPAXElement> T getParticipant(Set<? extends BioPAXElement> features,
	                                                  BioPAXElement cellularLocation,
	                                                  BioPAXElement bpPhysicalEntity)
	{
		if (bpLevel == BioPAXLevel.L2)
		{
			sequenceParticipant toReturn =
					bpModel.addNew(sequenceParticipant.class, genUri(sequenceParticipant.class));
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
				if (cellularLocation != null && bpPhysicalEntity != null) {
					// go through these hoops to keep EntryMapper from juggling different L3 ControlledVocabulary types
					if (bpModel.contains(cellularLocation)) {
						bpModel.remove(cellularLocation);
					}
					CellularLocationVocabulary clv = bpModel
							.addNew(CellularLocationVocabulary.class, cellularLocation.getRDFId());
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
	 * @param name               String
	 * @param shortName          String
	 * @param synonyms           Set<String>
	 * @param bpXrefs            Set<? extends BioPAXElement>
	 * @param bioSource          BioPAXElement
	 * @param sequence           String
	 * @return
	 */
	public <T extends BioPAXElement> T getPhysicalEntity(String physicalEntityType, 
	                                                     String name, String shortName,
	                                                     Set<String> synonyms,
	                                                     Set<? extends BioPAXElement> bpXrefs,
	                                                     BioPAXElement bioSource,
	                                                     String sequence)
	{

		if (bpLevel == BioPAXLevel.L2)
		{
			physicalEntity toReturn = null;
			if (physicalEntityType != null && physicalEntityType.equalsIgnoreCase("small molecule"))
			{
				toReturn = bpModel.addNew(smallMolecule.class, genUri(smallMolecule.class));
			}
			else if (physicalEntityType != null && physicalEntityType.equalsIgnoreCase("dna"))
			{
				toReturn = bpModel.addNew(dna.class, genUri(dna.class));
			}
			else if (physicalEntityType != null && physicalEntityType.equalsIgnoreCase("rna"))
			{
				toReturn = bpModel.addNew(rna.class, genUri(rna.class));
			}
			else
			{
				// default to protein
				toReturn = bpModel.addNew(protein.class, genUri(protein.class));
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
				toReturn = bpModel.addNew(SmallMolecule.class, genUri(SmallMolecule.class));
				er = bpModel.addNew(SmallMoleculeReference.class, genUri(SmallMoleculeReference.class));
			}
			else if (physicalEntityType != null && physicalEntityType.equalsIgnoreCase("dna"))
			{
				toReturn = bpModel.addNew(Dna.class, genUri(Dna.class));
				er = bpModel.addNew(DnaReference.class, genUri(DnaReference.class));
			}
			else if (physicalEntityType != null && physicalEntityType.equalsIgnoreCase("rna"))
			{
				toReturn = bpModel.addNew(Rna.class, genUri(Rna.class));
				er = bpModel.addNew(RnaReference.class, genUri(RnaReference.class));
			}
			else
			{
				// default to protein
				toReturn = bpModel.addNew(Protein.class, genUri(Protein.class));
				er = bpModel.addNew(ProteinReference.class, genUri(ProteinReference.class));
			}


			if (name != null)
			{
                er.setStandardName(name);
				toReturn.setStandardName(name);
			}
			if (shortName != null)
			{
                er.setDisplayName(shortName);
				toReturn.setDisplayName(shortName);
			}
			if (synonyms != null && synonyms.size() > 0)
			{
				for (String synonym : synonyms) {
                    er.addName(synonym);
					toReturn.addName(synonym);
				}
			}
			if (bpXrefs != null && bpXrefs.size() > 0)
			{
				for (BioPAXElement xref : bpXrefs) {
                    er.addXref((Xref) xref);
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
	 * @return
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
	 * Adds feature attributes to given existing sequence or entity feature.
	 *
	 * @param bpFeature BioPAXElement
	 * @param bpXrefs           Set<? extends BioPAXElement>
	 * @param featureLocations  Set<? extends BioPAXElement>
	 * @param featureType       BioPAXElement
	 * @return
	 */
	private <T extends BioPAXElement> T getFeature(BioPAXElement bpFeature,
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
			// TODO: EntityFeature does not implement XReferrable; what to do with bpXrefs?
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
	 * Gets a sequence/entity feature.
	 *
	 * @param bpXrefs          Set<? extends BioPAXElement>
	 * @param featureLocations Set<? extends BioPAXElement>
	 * @param featureType      BioPAXElement
	 * 
	 * @return
	 */
	public <T extends BioPAXElement> T getFeature(Set<? extends BioPAXElement> bpXrefs,
	                                              Set<? extends BioPAXElement> featureLocations,
	                                              BioPAXElement featureType)
	{		
		BioPAXElement firstXref = null;		
		// lets use xref id as id for feature - to eliminate duplicate features
		if(bpXrefs != null && !bpXrefs.isEmpty()) {
			//get by chance but still ok...
			firstXref = bpXrefs.iterator().next();
			//try to find and reuse the feature
			String id = getNamespace() + "SF-" + getXrefID(firstXref);
			BioPAXElement bpSequenceFeature = bpModel.getByID(id);
			if (bpSequenceFeature != null) {
				return getFeature(bpSequenceFeature, bpXrefs, featureLocations, featureType);
			}
		}
		
		//go ahead and create
		T toReturn = null;
		
		if (bpLevel == BioPAXLevel.L2)
		{
			sequenceFeature feature;
			if (firstXref != null) //also means bpXrefs not empty
			{
				String id = getNamespace() + "SF-" + getXrefID(firstXref);
				feature = bpModel.addNew(sequenceFeature.class, id);
				feature.setXREF((Set<xref>) bpXrefs);
			} else {
				feature = bpModel.addNew(sequenceFeature.class, genUri(sequenceFeature.class));
			}	
			
			if (featureLocations != null)
			{
				feature.setFEATURE_LOCATION((Set<sequenceLocation>) featureLocations);
			}
			
			if (featureType != null)
			{
				feature.setFEATURE_TYPE((openControlledVocabulary) featureType);
			}
			
			toReturn = (T) feature;
			
		} else if (bpLevel == BioPAXLevel.L3)
		{
			EntityFeature feature = bpModel.addNew(EntityFeature.class, genUri(EntityFeature.class));
			// TODO: EntityFeature does not implement XReferrable; what to do with bpXrefs?
			if (featureLocations != null)
			{
				for (BioPAXElement featureLocation : featureLocations) {
					feature.setFeatureLocation((SequenceLocation) featureLocation);
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
						bpModel.addNew(SequenceRegionVocabulary.class, featureType.getRDFId());
				replaceControlledVocabulary((ControlledVocabulary) featureType,
						srv);
				feature.setFeatureLocationType(srv);
			}
			
			toReturn = (T) feature;
		}

		return toReturn;
	}

	/**
	 * Gets a sequence location.
	 *
	 * @param beginSequenceInterval long
	 * @param endSequenceInterval   long
	 * @return <T extends BioPAXElement>
	 */
	public <T extends BioPAXElement> T getSequenceLocation(long beginSequenceInterval,
	                                                       long endSequenceInterval)
	{
		if (bpLevel == BioPAXLevel.L2)
		{
			sequenceInterval toReturn =
					bpModel.addNew(sequenceInterval.class, genUri(sequenceInterval.class));
			sequenceSite bpSequenceSiteBegin =
					bpModel.addNew(sequenceSite.class, genUri(sequenceSite.class));
			bpSequenceSiteBegin.setSEQUENCE_POSITION((int) beginSequenceInterval);
			toReturn.setSEQUENCE_INTERVAL_BEGIN(bpSequenceSiteBegin);
			sequenceSite bpSequenceSiteEnd =
					bpModel.addNew(sequenceSite.class, genUri(sequenceSite.class));
			bpSequenceSiteEnd.setSEQUENCE_POSITION((int) endSequenceInterval);
			toReturn.setSEQUENCE_INTERVAL_END(bpSequenceSiteEnd);
			
			return (T) toReturn;
		}
		else if (bpLevel == BioPAXLevel.L3)
		{
			SequenceInterval toReturn =
					bpModel.addNew(SequenceInterval.class, genUri(SequenceInterval.class));
			SequenceSite bpSequenceSiteBegin =
					bpModel.addNew(SequenceSite.class, genUri(SequenceSite.class));
			bpSequenceSiteBegin.setSequencePosition((int) beginSequenceInterval);
			toReturn.setSequenceIntervalBegin(bpSequenceSiteBegin);
			SequenceSite bpSequenceSiteEnd =
					bpModel.addNew(SequenceSite.class, genUri(SequenceSite.class));
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

	
	public String getNamespace() {
		return xmlBase;
	}

	/**
	 * Adds xrefs to given interaction.
	 *
	 * @param interaction Object
	 * @param bpXrefs Set<BioPAXElement>
	 */
	public <T extends BioPAXElement> void addXrefsToInteraction(T interaction, 
			Set<? extends BioPAXElement> bpXrefs)
	{
		if (bpLevel == BioPAXLevel.L2)
		{
            for (BioPAXElement bpXref : bpXrefs) {
                if(bpXref instanceof xref)
                    ((physicalInteraction) interaction).addXREF((xref) bpXref);
            }
		}
		else if (bpLevel == BioPAXLevel.L3)
		{
            for (BioPAXElement bpXref : bpXrefs) {
                if(bpXref instanceof Xref)
                    ((MolecularInteraction) interaction).addXref((Xref) bpXref);
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
			current.addXref(xref); //TODO: warn: attempts to add duplicate (URI) xrefs, which is ignored quietly...
		}
		if (vocabularyL3.contains(previous))
		{
			vocabularyL3.remove(previous);
		}
		vocabularyL3.add(current);
	}
	
	/**
	 * Generates a URI of a BioPAX object
	 * using the xml base, model interface name 
	 * and randomly generated long integer.
	 *
	 * @return String
	 */
	private String genUri(Class<? extends BioPAXElement> type) {
		return getNamespace() + type.getSimpleName()
				+ Long.toString(random.nextLong());
	}

}
