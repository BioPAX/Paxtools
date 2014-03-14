// $Id: EntryMapper.java,v 1.2 2009/11/23 13:59:42 rodche Exp $
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;

import psidev.psi.mi.xml.model.*;


/**
 * A thread class which processes an entry in a psi xml doc.
 * This class returns a paxtools model to a BioPAXMarshaller,
 * whose ref is passed during object construction.
 *
 * @author Benjamin Gross, rodche (re-factored to Runnable)
 */
class EntryMapper implements Runnable {

	/**
	 * Genetic Interactions.
	 */
	private static final ArrayList<String> GENETIC_INTERACTIONS;
	static {
		GENETIC_INTERACTIONS = new ArrayList<String>();
		GENETIC_INTERACTIONS.add("dosage growth defect");
		GENETIC_INTERACTIONS.add("dosage lethality");
		GENETIC_INTERACTIONS.add("dosage rescue");
		GENETIC_INTERACTIONS.add("negative genetic");
		GENETIC_INTERACTIONS.add("phenotypic enhancement");
		GENETIC_INTERACTIONS.add("phenotypic suppression");
		GENETIC_INTERACTIONS.add("positive genetic");
		GENETIC_INTERACTIONS.add("synthetic growth defect");
		GENETIC_INTERACTIONS.add("synthetic haploinsufficiency");
		GENETIC_INTERACTIONS.add("synthetic lethality");
		GENETIC_INTERACTIONS.add("synthetic rescue");
	}
	// as of BioGRID v3.1.72 (at least), genetic interaction code can reside
	// as an attribute of the Interaction via "BioGRID Evidence Code" key
	private static final String BIOGRID_EVIDENCE_CODE = "BioGRID Evidence Code";

	private final BioPAXMapper bpMapper;
	
	private final Entry entry;
	
	private final BioPAXMarshaller biopaxMarshaller;

	/**
	 * Ref to interatorMap
	 * (key is the interactor id, and the value is the Interactor)
	 */
	private Map<String, Interactor> interactorMap;

	/**
	 * Ref to experimentMap
	 * (key is the experiment description, and the value is the ExperimentDescription)
	 */
	private Map<Integer, ExperimentDescription> experimentMap;

	/**
	 * Set of open/controlled vocabulary.
	 */
	Set<BioPAXElement> vocabulary;

	/**
	 * Constructor.
	 *
	 * @param bpLevel
	 * @param xmlBase
	 * @param biopaxMarshaller BioPAXMarshaller
	 * @param entry EntrySet.Entry
	 */
	public EntryMapper(BioPAXLevel bpLevel, String xmlBase, BioPAXMarshaller biopaxMarshaller, Entry entry) {
		this.entry = entry;
		this.bpMapper = new BioPAXMapper(bpLevel, xmlBase);
		this.biopaxMarshaller = biopaxMarshaller;
	}

	/**
	 * Our implementation of run.
	 */
	public void run() {

		// set interactor type map
		interactorMap = createInteractorMap(entry);

		// create set of experiment information (evidence)
		experimentMap = createExperimentMap(entry);
		
		// get entry source name to add to interactions
		String entryDataSourceName = null;
		if (entry.hasSource() && entry.getSource().hasNames()) {
			entryDataSourceName = getName(entry.getSource().getNames());
		}

		// get availability 
		Set<String> availabilitySet = new HashSet<String>();
		if (entry.hasAvailabilities()) {
			for (Availability availability : entry.getAvailabilities()) {
				if (availability.hasValue()) {
					availabilitySet.add(availability.getValue());
				}
			}
		}

		// iterate through the interactions
		for (Interaction interaction : entry.getInteractions()) {

			// create the paxserve interaction
			createInteraction(entryDataSourceName,
							  availabilitySet,
							  interaction);
		}
		
		// add the model to the shared (by multiple threads) marshaller
		biopaxMarshaller.addModel(bpMapper.getModel());
	}

	/**
	 * Given an Entry, creates a hashmap of Interactors,
	 * where the key is the interactor id, and the value is the Interactor.
	 *
	 * @param entry Entry
	 * @return Map<String, Interactor>
	 */
	private Map<String, Interactor> createInteractorMap(Entry entry) {

		// create our hashmap to return
		Map<String, Interactor> map = new HashMap<String, Interactor>();

		// get interactor list
		if (entry.getInteractors() != null) {
			for (Interactor interactor : entry.getInteractors()) {
				map.put(Integer.toString(interactor.getId()), interactor);
			}
		}

		// outta here
		return map;
	}

	/**
	 * Given an EntrySet Entry, creates a hashmap of ExperimentTypes,
	 * where the key is the experiment description id, and the value is the ExperimentType.
	 *
	 * Note: We do this because as we interate over interactions, the interaction references
	 *       an experiment, and we can use the experiment id to reference the experiment description.
	 *
	 * @param entry Entry
	 * @return Map<Integer, ExperimentDescription>
	 */
	private Map<Integer, ExperimentDescription> createExperimentMap(Entry entry) {

		// create our hashmap to return
		Map<Integer, ExperimentDescription> map = new HashMap<Integer, ExperimentDescription>();

		// get experimentList
		if (entry.hasExperiments()) {
			for (ExperimentDescription experimentDescription : entry.getExperiments()) {
				map.put(new Integer(experimentDescription.getId()), experimentDescription);
			}
		}

		// outta here
		return map;
	}

	/**
	 * Creates a paxtools interaction object.
	 *
	 * Note:
	 *
	 * psi.interactionElementType                 -> biopax.(physicalInteraction or MolecularInteraction)
	 * psi.interactionElementType.participantList -> biopax.physicalInteraction.participants
	 *
	 * @param entryDataSourceName String
	 * @param availability Set<String>
	 * @param interaction Interaction
	 */
	private void createInteraction(String entryDataSourceName,
								   Set<String> availability,
								   Interaction interaction) {

		// a map between psi-mi Participant and biopax participant - required below in getExperimentalData()
		Map<Participant, BioPAXElement> psimiParticipantToBiopaxParticipantMap =
			new HashMap<Participant, BioPAXElement>();

		// as of BioGRID v3.1.72 (at least), genetic interaction code can reside
		// as an attribute of the Interaction via "BioGRID Evidence Code" key
		if (interaction.hasAttributes()) {
			for (Attribute attribute : interaction.getAttributes()) {
				if (attribute.getName().equalsIgnoreCase(BIOGRID_EVIDENCE_CODE)) {
					String value = (attribute.hasValue()) ? attribute.getValue().toLowerCase() : "";
					if (GENETIC_INTERACTIONS.contains(value)) {
						return;
					}
				}
			}
		}

		// experiment data - get it here, because it will help us determine if interaction is genetic
		Set<BioPAXElement> bpEvidence = getExperimentalData(interaction, psimiParticipantToBiopaxParticipantMap);

		// don't add genetic interactions to file (at least biogrid will be affected 1/6/09)
		if (bpMapper.isGeneticInteraction(GENETIC_INTERACTIONS, bpEvidence)) return;

		// get interaction name/short name
		String name = null;
		String shortName = null;
		if (interaction.hasNames()) {
			Names names = interaction.getNames();
			name = (names.hasFullName()) ? names.getFullName() : "";
			shortName = (names.hasShortLabel()) ? names.getShortLabel() : "";
		}

		// interate through the psi participants, create biopax equivalents
		Set<BioPAXElement> bpParticipants = new HashSet<BioPAXElement>();
		for (Participant participant : interaction.getParticipants()) {
			// get paxtools physical entity participant and add to participant list
			BioPAXElement bpParticipant = createParticipant(participant);
			if (bpParticipant != null) {
				bpParticipants.add(bpParticipant);
				psimiParticipantToBiopaxParticipantMap.put(participant, bpParticipant);
			}
		}

        // interaction publication & unification xref 
		Set<BioPAXElement> bpXrefs = new HashSet<BioPAXElement>();
		if (entry.hasSource() && entry.getSource().hasBibref()) {
			bpXrefs.addAll(getPublicationXref(entry.getSource().getBibref().getXref()));
		}
		if (interaction.hasXref()) {
			bpXrefs.addAll(getXrefs(interaction.getXref(), true));
		}

		BioPAXElement bpInteraction = bpMapper.getInteraction(name, shortName,
															  availability,
															  bpParticipants,
															  bpEvidence);

		bpMapper.addXrefsToInteraction(bpInteraction, bpXrefs);
	}

	/**
	 * Creates a paxtools participant.
	 *
	 * Note:
	 *
	 * psi.participantType -> biopax.(physicalEntityParticipant or PhysicalEntity)
	 *
	 * @param participant Participant
	 *
	 * @return BioPAXElement
	 */
	private BioPAXElement createParticipant(Participant participant) {

		// features
		Set<BioPAXElement> features = getFeatureList(participant.getFeatures());
	
		// get protein interactor type
		// use the interactor ref to get the interactor out of the interactor list
		String interactorRef = "";
		Interactor interactor = null;
		if (participant.hasInteractorRef()) {
			interactorRef = Integer.toString(participant.getInteractorRef().getRef());
			interactor = interactorMap.get(interactorRef);
		}
		else if (participant.hasInteractor()) {
			interactor = participant.getInteractor();
			interactorRef = Integer.toString(interactor.getId());
		}

		// houston we have a problem
		if (interactor == null || interactorRef.length() == 0) {
			System.err.println("EntryMapper.createParticipant(): Error - interactor or interactor ref cannot be found");
			System.err.println("participant: " + participant.toString());
			return null;
		}

		// cellular location
		BioPAXElement cellularLocation =
			getOpenControlledVocabulary((interactor.hasOrganism() &&
										 interactor.getOrganism().hasCompartment()) ?
										interactor.getOrganism().getCompartment() : null);

		// create the physical entity which is contained within the participant, if it does not already exist
		String physicalEntityRdfId = bpMapper.getNamespace() + interactorRef;
		BioPAXElement bpPhysicalEntity = bpMapper.getBioPAXElement(physicalEntityRdfId);
		bpPhysicalEntity = (bpPhysicalEntity == null) ?
			createPhysicalEntity(physicalEntityRdfId, interactor) : bpPhysicalEntity;

		// outta here
		return bpMapper.getParticipant(features, cellularLocation, bpPhysicalEntity);
	}

	/**
	 * Creates a paxtools physical entity.
	 *
	 * Note:
	 *
	 * psi.interactorElementType  -> biopax.physicalEntity
	 *
	 * @param physicalEntityRdfId String
	 * @param interactor Interactor
	 *
	 * @return BioPAXElement
	 */
	private BioPAXElement createPhysicalEntity(String physicalEntityRdfId,
											   Interactor interactor) {

		// figure out physical entity type (protein, dna, rna, small molecule)
		String physicalEntityType = null;
		CvType interactorType = interactor.getInteractorType();
		if (interactorType != null && interactorType.hasNames()) {
			physicalEntityType = getName(interactorType.getNames());
		}

		// get names/synonyms
		String name = null;
		String shortName = null;
		Set<String> synonyms = new HashSet<String>();
		Names psiNames = interactor.getNames();
		if (psiNames != null) {
			name = (psiNames.hasFullName()) ? psiNames.getFullName() : null;
			shortName = (psiNames.hasShortLabel()) ? psiNames.getShortLabel() : null;
			if (psiNames.hasAliases()) {
				for (Alias alias : psiNames.getAliases()) {
					if (alias.hasValue()) {
						synonyms.add(alias.getValue());
					}
				}
			}
		}

		return bpMapper.getPhysicalEntity(physicalEntityType,
										  name, shortName, synonyms,
										  getXrefs(interactor.getXref(), false),
										  getBioSource(interactor.getOrganism()),
										  interactor.getSequence());
	}

	/**
	 * Creates a list of paxtools elements given a psi feature list
	 *
	 * @param psiFeatureList Collection<Feature>
	 *
	 * @return Set<BioPAXElement>
	 */
	private Set<BioPAXElement> getFeatureList(Collection<Feature> psiFeatureList) {

		// check args
		if (psiFeatureList == null || psiFeatureList.size() == 0) return null;

		// set to return
		Set<BioPAXElement> toReturn = new HashSet<BioPAXElement>();

		// interate through psi feature list
		for (Feature psiFeature : psiFeatureList) {

			// feature location
			Set<BioPAXElement> sequenceLocationSet =
				getSequenceLocation(psiFeature.getRanges());

			// feature type
			BioPAXElement bpFeatureType = null;
			if (psiFeature.hasFeatureType()) {
				bpFeatureType = getOpenControlledVocabulary(psiFeature.getFeatureType());
            }

			// xref - use feature type xref
			Xref psiFeatureXref = psiFeature.getXref();
			Set<BioPAXElement> bpSequenceFeatureXref = getXrefs(psiFeatureXref, false);// null/empty is ok too
			//using the bpSequenceFeatureXref, it will try getting the feature while avoiding duplicates
			toReturn.add(bpMapper.getFeature(bpSequenceFeatureXref, sequenceLocationSet, bpFeatureType));
		}

		return toReturn;
	}

	/**
	 * Given a psiFeature, return the proper sequenceLocation.
	 *
	 * @param rangeList Collection<Range>
	 * @return Set<BioPAXElement>
	 */
	private Set<BioPAXElement> getSequenceLocation(Collection<Range> rangeList) {

		// set tot return
		Set<BioPAXElement> toReturn = new HashSet<BioPAXElement>();

		// if we have a locationType, lets process
		for (Range range : rangeList) {

			// get begin & end interval
			Interval beginInterval = (range.hasBeginInterval()) ?
				range.getBeginInterval() : null;
			Interval endInterval = (range.hasEndInterval()) ?
				range.getEndInterval() : null;

			if (beginInterval == null) {
				continue;
			}
			else {
				toReturn.add(bpMapper.getSequenceLocation(beginInterval.getBegin(), beginInterval.getEnd()));
			}

			if (endInterval != null) {
				toReturn.add(bpMapper.getSequenceLocation(endInterval.getBegin(), endInterval.getEnd()));
			}
		}

		// outta here
		return toReturn;
	}


	/**
	 * Given a psi organism, return a paxtools biosource.
	 *
	 * @param organism Organism
	 * @return BioPAXElement
	 */
	private BioPAXElement getBioSource(Organism organism) {

		// check args
		if (organism == null) return null;

		// set the BioPXElement URI and taxonomy xref id
		String ncbiId = Integer.toString(organism.getNcbiTaxId());
		String bioSourceUri = bpMapper.getNamespace() + "BS-" + ncbiId;

		// outta here if element already exists in model
		BioPAXElement bpBioSource = bpMapper.getBioPAXElement(bioSourceUri);
		if (bpBioSource != null) return bpBioSource;

		// taxon xref
		BioPAXElement taxonXref = bpMapper.addNewUnificationXref(null); //generate URI
		bpMapper.setXrefDBAndID(taxonXref, "Taxonomy", ncbiId);

		// cell type
		BioPAXElement cellType = getOpenControlledVocabulary(organism.getCellType());

		// tissue
		BioPAXElement tissue = getOpenControlledVocabulary(organism.getTissue());

		String bioSourceName = null;
		if (organism.hasNames()) {
			bioSourceName = getName(organism.getNames());
		}

		return bpMapper.getBioSource(bioSourceUri, taxonXref, cellType, tissue, bioSourceName);
	}

	/**
	 * Given an OpenCvType, return a paxtools openControlledVocabulary.
	 * 
	 * @param openCvType OpenCvType
	 * @return BioPAXElement
	 */
	private BioPAXElement getOpenControlledVocabulary(OpenCvType openCvType) {

		// check args
		if (openCvType == null) return null;

		// outta here
		return getOpenControlledVocabulary((CvType)openCvType);
	}

	/**
	 * Given a CvType, return a paxtools openControlledVocabulary.
	 *
	 * @param cvType CvType
	 * @return BioPAXElement
	 */
	private BioPAXElement getOpenControlledVocabulary(CvType cvType) {

		if (cvType == null) return null;

		// try full name first, else try short name
		String nameToSearch = null;
		if (cvType.hasNames()) {
			nameToSearch = getName(cvType.getNames());
			if (nameToSearch == null) return null;
		}

		// look for name in our vocabulary set
		BioPAXElement toReturn = bpMapper.getOpenControlledVocabulary(nameToSearch);
		if (toReturn != null) return toReturn;

		// made it here, we have to create a new open/controlled vocabulary
		Set<BioPAXElement> bpXrefs = getXrefs(cvType.getXref(), true);
		toReturn = bpMapper.getOpenControlledVocabulary(nameToSearch, bpXrefs);

		return toReturn;
 	}

	/**
	 * Given a psi xref, returns paxtools unification and relationship xrefs.
	 *
	 * @param psiXREF Xref
	 * @param forOCVorInteraction boolean
	 * @return
	 */
	private Set<BioPAXElement> getXrefs(Xref psiXREF, boolean forOCVorInteraction) {

		// set to return
		Set<BioPAXElement> toReturn = new HashSet<BioPAXElement>();

		// check args
		if (psiXREF == null) return toReturn;

		// create list of all references
		List<DbReference> psiDBRefList = new ArrayList<DbReference>();
		psiDBRefList.add(psiXREF.getPrimaryRef());
		if (psiXREF.hasSecondaryRef()) {
			psiDBRefList.addAll(psiXREF.getSecondaryRef());
		}

		for (DbReference psiDBRef : psiDBRefList) {
			// check for null xref
			if (psiDBRef == null) continue;

			// process ref type
			BioPAXElement bpXref = null;
			String refType = (psiDBRef.hasRefType()) ? psiDBRef.getRefType() : null;
            String psiDBRefId = psiDBRef.getId();

            // If multiple ids given with comma separated values, then split them.
            for (String dbRefId : psiDBRefId.split(",")) 
            {
                if (refType != null 
                	&& (refType.equals("identity") || refType.equals("identical object"))) 
                {
                    String id = bpMapper.getNamespace() + "UXR-" + validateDBID(dbRefId);
                    bpXref = bpMapper.getBioPAXElement(id);                   
                    if (bpXref != null) {
                        toReturn.add(bpXref);
                        continue;
                    }                    
                    
                    bpXref = bpMapper.addNewUnificationXref(id);
                    
                } 
                else if (!forOCVorInteraction) 
                {
                    String id = bpMapper.getNamespace() + "RXR-" + validateDBID(dbRefId);
                    bpXref = bpMapper.getBioPAXElement(id);                   
                    if (bpXref != null) {
                        toReturn.add(bpXref);
                        continue;
                    }                    
                    bpXref = (refType != null) 
                    	? bpMapper.getRelationshipXref(id, refType) 
                    	: (	
                    		psiDBRef.getDb().toLowerCase().equals("uniprot")
                    			? bpMapper.addNewUnificationXref(id) : bpMapper.getRelationshipXref(id, null)
                    	  );
                }

                //set properties for the new xref and add it to the set to return
                if (bpXref != null) {
                    bpMapper.setXrefDBAndID(bpXref, psiDBRef.getDb(), dbRefId);
                    toReturn.add(bpXref);
                }
            }
        }

		return toReturn;
	}

	/**
	 * Given a psi xref, returns a paxtools xref.
	 *
	 * @param psiXREF Xref
	 * @return Set<BioPAXElement>
	 */
	private Set<BioPAXElement> getPublicationXref(Xref psiXREF) {

		// set to return
		Set<BioPAXElement> toReturn = new HashSet<BioPAXElement>();

		if (psiXREF == null) return toReturn;

		// get primary 
		DbReference psiDBRef = psiXREF.getPrimaryRef();
		if (psiDBRef == null) return toReturn;

		// create publication ref
		String id = bpMapper.getNamespace() + "PXR-" + validateDBID(psiDBRef.getId());
		BioPAXElement bpXref = bpMapper.getBioPAXElement(id);
		// outta here if element already exists in model
		if (bpXref != null) {
			toReturn.add(bpXref);
			return toReturn;
		}
		else {
			bpXref = bpMapper.getPublicationXref(id);
		}
		bpMapper.setXrefDBAndID(bpXref, psiDBRef.getDb(), psiDBRef.getId());
		
		// outta here
		toReturn.add(bpXref);
		return toReturn;
	}

	/**
	 * Validate an xref id from a psi-mi file.
	 * Replaces: reserved html chars with '-'.
	 */
	private String validateDBID(String id) {
		return id.replaceAll("\\$|\\&|\\+|,|/|:|;|=|\\?|@| ", "-");
	}

	/**
	 * Given an interaction, return a set of paxtools evidence objects.
	 *
	 * @param interaction Interaction
	 * @param psimiParticipantToBiopaxParticipantMap Map<Participant, BioPAXElement>
	 * @return Set<T>
	 */
	private Set<BioPAXElement> getExperimentalData(Interaction interaction,
												   Map<Participant, BioPAXElement> psimiParticipantToBiopaxParticipantMap) {

		// set to return
		Set<BioPAXElement> toReturn = new HashSet<BioPAXElement>();

		// get experiment list
		Collection<?> experimentList = new ArrayList<Object>();
		if (interaction.hasExperiments()) {
			experimentList = interaction.getExperiments();
		}
		else if (interaction.hasExperimentRefs()) {
			experimentList = interaction.getExperimentRefs();
		}
		
		for (Object o : experimentList) {
			// get ref to experiment type
			ExperimentDescription experimentDescription = (interaction.hasExperiments()) ?
				(ExperimentDescription)o : experimentMap.get(((ExperimentRef)o).getRef());
			if (experimentDescription != null) {
				// create comment set - used to capture name/attributes
				Set<String> comments = new HashSet<String>();
				// name
				if (experimentDescription.hasNames()) {
					String name = getName(experimentDescription.getNames());
					if (name != null) comments.add(name);
				}
				// bibref / xref
				Set<BioPAXElement> bpXrefs = new HashSet<BioPAXElement>();
				if (experimentDescription.hasXref()) {
					bpXrefs.addAll(getXrefs(experimentDescription.getXref(), false));
				}
				if (experimentDescription.getBibref() != null) {
					bpXrefs.addAll(getPublicationXref(experimentDescription.getBibref().getXref()));
				}
				// host organism list dropped
				// interaction detection method, participant detection method, feature detection method
				Set<BioPAXElement> evidenceCodes = getEvidenceCodes(experimentDescription);
				// confidence list
				Set<BioPAXElement> scoresOrConfidences = new HashSet<BioPAXElement>();
				if (experimentDescription.hasConfidences()) {
					for (Confidence psiConfidence : experimentDescription.getConfidences()) {
						BioPAXElement bpScoreOrConfidence = getScoreOrConfidence(psiConfidence);
						if (bpScoreOrConfidence != null) scoresOrConfidences.add(bpScoreOrConfidence);
					}
				}
				// attribute list
				if (experimentDescription.hasAttributes()) {
					comments.addAll(getAttributes(experimentDescription.getAttributes()));
				}
				// experimental form
				Set<BioPAXElement> experimentalForms = getExperimentalFormSet(experimentDescription, interaction,
																			  psimiParticipantToBiopaxParticipantMap);
				// add evidence to list we are returning
				toReturn.add(bpMapper.getEvidence(bpXrefs, evidenceCodes,
												  scoresOrConfidences, comments, experimentalForms));
			}
		}

		// outta here
		return toReturn;
	}

	/**
	 * Given a psi-mi experiment type, returns a set of open
	 * controlled vocabulary objects which represent evidence code(s).
	 *
	 * @param experimentDescription Experiment
	 * @return Set<BioPAXElement>
	 */
	private Set<BioPAXElement> getEvidenceCodes(ExperimentDescription experimentDescription) {

		// set to return
		Set<BioPAXElement> toReturn = new HashSet<BioPAXElement>();

		// get experiment methods
		Set<CvType> cvTypeSet = new HashSet<CvType>(3);
		cvTypeSet.add(experimentDescription.getInteractionDetectionMethod());
		cvTypeSet.add(experimentDescription.getParticipantIdentificationMethod());
		cvTypeSet.add(experimentDescription.getFeatureDetectionMethod());

		// create openControlledVocabulary objects for each detection method
		for (CvType cvtype : cvTypeSet) {
			if (cvtype == null) continue;
			BioPAXElement ocv = getOpenControlledVocabulary(cvtype);
			if (ocv != null) toReturn.add(ocv);
		}

		// outta here
		return toReturn;
	}

	/**
	 * Given a psi-mi confidence object, returns a paxtools confidence object.
	 *
	 * @param psiConfidence Confidence
	 * @return BioPAXElement
	 */
	private BioPAXElement getScoreOrConfidence(Confidence psiConfidence) {

		// check args
		if (psiConfidence == null) return null;

		// psiConfidence.value maps to confidence.confidence-value
		String value = psiConfidence.getValue();

		// get psiConfidence unit
		OpenCvType ocv = psiConfidence.getUnit(); 

		// psiConfidence.unit.xref maps to confidence.xref
		Set<BioPAXElement> bpXrefs = new HashSet<BioPAXElement>();
		if (ocv != null && ocv.getXref() != null) {
			bpXrefs.addAll(getXrefs(ocv.getXref(), false));
		}

		// used to store names and attributes
		Set<String> comments = new HashSet<String>();

		// psiConfidence.unit.names maps to confidence comment
		if (ocv != null && ocv.getNames() != null) {
			String unitName = getName(ocv.getNames());
			if (unitName != null) comments.add(unitName);
		}

		// unit.attributelist maps to confidence.comment
		if (ocv.hasAttributes()) {
			comments.addAll(getAttributes(ocv.getAttributes()));
		}

		// outta here
		return (bpMapper.getScoreOrConfidence(value, bpXrefs, comments));
	}

	/**
	 * Given a psi-mi attributes list, returns a string set, where
	 * each string is concatenation of name/value pairs.
	 *
	 * @param attributes Collection<Attribute>
	 * @return Set<String>
	 */
	private Set<String> getAttributes(Collection<Attribute> attributes) {

		// set to return
		Set<String> toReturn = new HashSet<String>();

		// iterate over the attributes
		for (Attribute attribute : attributes) {
			String attributeStr = "";
			if (attribute.hasValue()) {
				attributeStr = attribute.getValue();
			}
			String name = attribute.getName();
			attributeStr = (name != null) ? attributeStr += " " + name : attributeStr;
			if (attribute.hasNameAc()) {
				attributeStr += " " + attribute.getNameAc();
			}
			if (attributeStr.length() > 0) toReturn.add(attributeStr);
		}

		// outta here
		return toReturn;
	}

	/**
	 * Given a psi-mi interaction element type, returns a set of experimental forms.
	 *
	 * @param experimentDescription ExperimentDescription
	 * @param interaction Interaction
	 * @param psimiParticipantToBiopaxParticipantMap Map<Participant, BioPAXElement>
	 * @return Set<BioPAXElement>
	 */
	private Set<BioPAXElement> getExperimentalFormSet(ExperimentDescription experimentDescription,
													  Interaction interaction,
													  Map<Participant, BioPAXElement> psimiParticipantToBiopaxParticipantMap) {

		// set to return
		Set<BioPAXElement> toReturn = new HashSet<BioPAXElement>();
		Set<String> processedRoles = new HashSet<String>();

		// interate through the psi participants, get experimental role
		for (Participant participant : interaction.getParticipants()) {
			// get participant - may be used in following loop
			BioPAXElement bpParticipant = psimiParticipantToBiopaxParticipantMap.get(participant.getId());
			// get experimental role list
			if (participant.hasExperimentalRoles()) {
				for (ExperimentalRole experimentalRole : participant.getExperimentalRoles()) {
					// get experimental ref list &
					// determine if this participant plays a role in the experiment parameter of method
					boolean relevantExperimentalRole = false;
					// according to psi-mi spec, if no experimentalRef is given,  participant plays role in all experiments
					if (!experimentalRole.hasExperimentRefs()) {
						relevantExperimentalRole = true;
					}
					else {
						for (ExperimentRef experimentRef : experimentalRole.getExperimentRefs()) {
							ExperimentDescription thisExperimentDescription = experimentMap.get(experimentRef.getRef());
							if (thisExperimentDescription == experimentDescription) {
								relevantExperimentalRole = true;
								break;
							}
						}
					}
					if (relevantExperimentalRole) {
						// check that we havent already processed this role to prevent duplicate experimental forms
						if (experimentalRole.hasNames()) {
							String roleName = getName(experimentalRole.getNames());
							if (!processedRoles.contains(roleName)) {
								toReturn.add(bpMapper
									.getExperimentalForm(getOpenControlledVocabulary(experimentalRole), bpParticipant));
								processedRoles.add(roleName);
							}
						}
					}
				}
			}
		}

		// outta here
		return toReturn;
	}

	/**
	 * Given a Names object, returns a name string.
	 *
	 * @param name Names
	 * @return String
	 */
	private String getName(Names name) {
		
		if (name.hasFullName()) {
			return name.getFullName();
		}
		else if (name.hasShortLabel()) {
			return name.getShortLabel();
		}

		// outta here
		return null;
	}

}
