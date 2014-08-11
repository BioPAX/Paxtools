package org.biopax.paxtools.converter.psi;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.BindingFeature;
import org.biopax.paxtools.model.level3.BioSource;
import org.biopax.paxtools.model.level3.CellVocabulary;
import org.biopax.paxtools.model.level3.CellularLocationVocabulary;
import org.biopax.paxtools.model.level3.Complex;
import org.biopax.paxtools.model.level3.ControlledVocabulary;
import org.biopax.paxtools.model.level3.Dna;
import org.biopax.paxtools.model.level3.DnaReference;
import org.biopax.paxtools.model.level3.Entity;
import org.biopax.paxtools.model.level3.EntityFeature;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.Evidence;
import org.biopax.paxtools.model.level3.EvidenceCodeVocabulary;
import org.biopax.paxtools.model.level3.ExperimentalForm;
import org.biopax.paxtools.model.level3.ExperimentalFormVocabulary;
import org.biopax.paxtools.model.level3.InteractionVocabulary;
import org.biopax.paxtools.model.level3.MolecularInteraction;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.model.level3.PublicationXref;
import org.biopax.paxtools.model.level3.RelationshipTypeVocabulary;
import org.biopax.paxtools.model.level3.RelationshipXref;
import org.biopax.paxtools.model.level3.Rna;
import org.biopax.paxtools.model.level3.RnaReference;
import org.biopax.paxtools.model.level3.Score;
import org.biopax.paxtools.model.level3.SequenceEntityReference;
import org.biopax.paxtools.model.level3.SequenceInterval;
import org.biopax.paxtools.model.level3.SequenceLocation;
import org.biopax.paxtools.model.level3.SequenceRegionVocabulary;
import org.biopax.paxtools.model.level3.SequenceSite;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.model.level3.SmallMolecule;
import org.biopax.paxtools.model.level3.SmallMoleculeReference;
import org.biopax.paxtools.model.level3.TissueVocabulary;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.model.level3.Xref;

import psidev.psi.mi.xml.model.*;


/**
 * A thread class which processes an entry in a psi xml doc.
 * This class returns a paxtools model to a BioPAXMarshaller,
 * whose ref is passed during object construction.
 *
 * @author Benjamin Gross, rodche (full re-factoring for Level3)
 */
class EntryMapper implements Runnable {

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
	
	private static final String IDENTIFIERS_ORG = "http://identifiers.org/";
	
	private Model bpModel;
	
	private final String xmlBase;
	
	private final Entry entry;
	
	private final BioPAXMarshaller biopaxMarshaller;
	
	private final AtomicLong counter;
	
	private final boolean forceInteractionToComplex;

	/*
	 * Ref to interatorMap
	 * (key is the interactor id, and the value is the Interactor)
	 */
	private Map<String, Interactor> interactorMap;

	/*
	 * Ref to experimentMap
	 * (key is the experiment description, and the value is the ExperimentDescription)
	 */
	private Map<Integer, ExperimentDescription> experimentMap;


	/**
	 * Constructor.
	 *
	 * @param xmlBase
	 * @param biopaxMarshaller
	 * @param entry
	 * @param forceInteractionToComplex - always generate Complex instead of MolecularInteraction
	 * @param rnd a random number generator (to generate URIs)
	 */
	public EntryMapper(String xmlBase, BioPAXMarshaller biopaxMarshaller, 
			Entry entry, boolean forceInteractionToComplex, AtomicLong counter) {
		this.entry = entry;
		this.counter = counter;
		this.biopaxMarshaller = biopaxMarshaller;
		this.xmlBase = xmlBase;
		this.forceInteractionToComplex = forceInteractionToComplex;
	}


	public void run() {
		this.bpModel = BioPAXLevel.L3.getDefaultFactory().createModel();
		this.bpModel.setXmlBase(xmlBase);					

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
		
		// iterate through the interactions and create biopax/paxtools mol. interactions
		for (Interaction interaction : entry.getInteractions()) {
			processInteraction(entryDataSourceName, availabilitySet, interaction);
		}
		
		// add the model to the shared (by multiple threads) marshaller
		biopaxMarshaller.addModel(bpModel);
	}

	/*
	 * Given an Entry, creates a hashmap of Interactors,
	 * where the key is the interactor id, and the value is the Interactor.
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

		return map;
	}

	/*
	 * Given an EntrySet Entry, creates a hashmap of ExperimentTypes,
	 * where the key is the experiment description id, and the value is the ExperimentType.
	 *
	 * Note: We do this because as we interate over interactions, the interaction references
	 *       an experiment, and we can use the experiment id to reference the experiment description.
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

		return map;
	}

	/*
	 * Creates a paxtools object that
	 * corresponds to the psi interaction.
	 *
	 * Note:
	 *
	 * psi.interactionElementType                 -> biopax.(physicalInteraction or MolecularInteraction) TODO consider Complex
	 * psi.interactionElementType.participantList -> biopax.physicalInteraction.participants
	 */
	private void processInteraction(String entryDataSourceName,
								   Set<String> availability,
								   Interaction interaction) {
		// a map between psi-mi Participant and biopax participant - required below in getExperimentalData()
		Map<Participant, SimplePhysicalEntity> psimiParticipantToBiopaxParticipantMap =
				new HashMap<Participant, SimplePhysicalEntity>();

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
		Set<Evidence> bpEvidence = getExperimentalData(interaction, psimiParticipantToBiopaxParticipantMap);

		// don't add genetic interactions to file (at least biogrid will be affected 1/6/09)
		if (isGeneticInteraction(GENETIC_INTERACTIONS, bpEvidence)) {
			for(Evidence ev : bpEvidence)
				bpModel.remove(ev);
			return;
		}

		// get interaction name/short name
		String name = null;
		String shortName = null;
		if (interaction.hasNames()) {
			Names names = interaction.getNames();
			name = (names.hasFullName()) ? names.getFullName() : "";
			shortName = (names.hasShortLabel()) ? names.getShortLabel() : "";
		}

		// interate through the psi participants, create biopax equivalents
		Set<SimplePhysicalEntity> bpParticipants = new HashSet<SimplePhysicalEntity>();
		for (Participant participant : interaction.getParticipants()) {
			// get paxtools physical entity participant and add to participant list
			SimplePhysicalEntity bpParticipant = createParticipant(participant);
			if (bpParticipant != null) {
				bpParticipants.add(bpParticipant);
				psimiParticipantToBiopaxParticipantMap.put(participant, bpParticipant);
			}
		}

		
		boolean complex = false; // TODO have to decide whether to generate an Interaction or Complex 
		
		Set<InteractionVocabulary> interactionVocabularies = new HashSet<InteractionVocabulary>();
		if (interaction.hasInteractionTypes()) {
			for(CvType interactionType : interaction.getInteractionTypes()) {
				//generate InteractionVocabulary and set interactionType
				InteractionVocabulary cv = findOrCreateControlledVocabulary(interactionType, InteractionVocabulary.class);
				interactionVocabularies.add(cv);
				//TODO e.g. if terms were 'direct interaction' or 'physical association' (as in IntAct), set complex=true
			}
		}
	
		Entity bpEntity = null;
		
		if (complex || forceInteractionToComplex) {
			//TODO generate a Complex, add the components (participants)
			bpEntity = createComplex(
					name, shortName, availability, bpParticipants, bpEvidence);
		} else {
			bpEntity = createMolecularInteraction(
					name, shortName, availability, bpParticipants, bpEvidence);
			
			for(InteractionVocabulary iv : interactionVocabularies) {
				((MolecularInteraction) bpEntity).addInteractionType(iv);
			}
		}
		
		//TODO set bf.bindsTo symmetrical props (e.g., for IntAct Complex data, can use <inferredInteractionList> element)	
		//interaction.getInferredInteractions()...
		
		// add xrefs		
        // interaction publication & unification xref 
		Set<Xref> bpXrefs = new HashSet<Xref>();
		if (entry.hasSource() && entry.getSource().hasBibref()) {
			bpXrefs.addAll(getPublicationXref(entry.getSource().getBibref().getXref()));
		}
		if (interaction.hasXref()) {
			bpXrefs.addAll(getXrefs(interaction.getXref(), true));
		}
		
		for (Xref bpXref : bpXrefs) {
			bpEntity.addXref(bpXref);
		}
	}

	/*
	 * Creates a paxtools participant.
	 *
	 * Note:
	 * psi.participantType -> biopax.(physicalEntityParticipant or PhysicalEntity)
	 */
	private SimplePhysicalEntity createParticipant(Participant participant) {
	
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

		// we have a problem
		if (interactor == null || interactorRef.length() == 0) {
			System.err.println("EntryMapper.createParticipant(): Error - interactor or interactor ref cannot be found");
			System.err.println("participant: " + participant.toString());
			return null;
		}

		// cellular location
		CellularLocationVocabulary cellularLocation = findOrCreateControlledVocabulary(
			(interactor.hasOrganism() && interactor.getOrganism().hasCompartment()) 
				? interactor.getOrganism().getCompartment() : null, CellularLocationVocabulary.class);		
		
		// find or create the physical entity which is contained/referred within the participant
		SimplePhysicalEntity bpPhysicalEntity = createPhysicalEntity(interactor, interactorRef, 
				cellularLocation, participant.getFeatures());
				
		return bpPhysicalEntity;
	}

	/*
	 * Creates a paxtools simple physical entity.
	 *
	 * Note:
	 * psi.interactorElementType  -> PhysicalEntity (more specifically, SimplePhysicalEntity in paxtools)
	 */
	private SimplePhysicalEntity createPhysicalEntity(Interactor interactor, String interactorRef, 
			CellularLocationVocabulary cellularLocation, Collection<Feature> features) 
	{
		String physicalEntityRdfId = xmlBase + encode(interactorRef);
		
		SimplePhysicalEntity toReturn = (SimplePhysicalEntity) bpModel.getByID(physicalEntityRdfId);		
		if(toReturn != null) 
			return toReturn;
		
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
		
		Set<Xref> bpXrefs = getXrefs(interactor.getXref(), false);
		
		EntityReference er = null;
		if (physicalEntityType != null && physicalEntityType.equalsIgnoreCase("small molecule"))
		{
			toReturn = bpModel.addNew(SmallMolecule.class, genUri(SmallMolecule.class, bpModel));
			er = bpModel.addNew(SmallMoleculeReference.class, genUri(SmallMoleculeReference.class, bpModel));
		}
		else if (physicalEntityType != null && physicalEntityType.equalsIgnoreCase("dna"))
		{
			toReturn = bpModel.addNew(Dna.class, genUri(Dna.class, bpModel));
			er = bpModel.addNew(DnaReference.class, genUri(DnaReference.class, bpModel));
		}
		else if (physicalEntityType != null && physicalEntityType.equalsIgnoreCase("rna"))
		{
			toReturn = bpModel.addNew(Rna.class, genUri(Rna.class, bpModel));
			er = bpModel.addNew(RnaReference.class, genUri(RnaReference.class, bpModel));
		}
		else
		{
			// default to protein
			toReturn = bpModel.addNew(Protein.class, genUri(Protein.class, bpModel));
			er = bpModel.addNew(ProteinReference.class, genUri(ProteinReference.class, bpModel));
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
			for (Xref xref : bpXrefs) {
                er.addXref((Xref) xref);
			}
		}
		
		// set sequence entity ref props
		if (er instanceof SequenceEntityReference) {
			SequenceEntityReference ser = (SequenceEntityReference)er;
			ser.setOrganism(getBioSource(interactor.getOrganism()));
			ser.setSequence(interactor.getSequence());
		}
		
		// set entity ref on pe
		toReturn.setEntityReference(er);		
		
		// add features
		addFeatures(toReturn, features);		
		
		//set cellular loc.
		toReturn.setCellularLocation(cellularLocation);
		
		return toReturn;
	}

	/*
	 * Given a psi feature list,
	 * creates and adds biopax entity features 
	 * to the simple physical entity and its ent. ref.
	 */
	private void addFeatures(SimplePhysicalEntity pe, Collection<Feature> psiFeatureList) {

		// check args
		if (psiFeatureList == null || psiFeatureList.size() == 0) 
			return;

		// interate through psi feature list
		for (Feature psiFeature : psiFeatureList) {
			if(psiFeature==null) continue;
			
			//using the xrefs, it will try getting the feature while avoiding duplicates
			//TODO consider other types, e.g. ModificationFeature under some circumstances; perhaps use psimi <featureType>...
			Class<? extends EntityFeature> featureClass = (pe instanceof SmallMolecule)
					? BindingFeature.class : EntityFeature.class; 
			
			EntityFeature feature = getFeature(featureClass, psiFeature);			
			if(feature != null) {
				pe.addFeature(feature);
			}
			
			if(pe.getEntityReference()!=null) {//in fact, always...
				pe.getEntityReference().addEntityFeature(feature);
			}				
		}
	}

	/*
	 * Given a psiFeature, return the set
	 * of SequenceInterval (sequence locations). 
	 */
	private Set<SequenceInterval> getSequenceLocation(Collection<Range> rangeList) {
		Set<SequenceInterval> toReturn = new HashSet<SequenceInterval>();

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
				toReturn.add(getSequenceLocation(beginInterval.getBegin(), beginInterval.getEnd()));
			}

			if (endInterval != null) {
				toReturn.add(getSequenceLocation(endInterval.getBegin(), endInterval.getEnd()));
			}
		}

		return toReturn;
	}


	/*
	 * Given a psi organism, return a paxtools biosource.
	 */
	private BioSource getBioSource(Organism organism) {

		// check args
		if (organism == null) return null;

		// set the BioPXElement URI and taxonomy xref id
		String ncbiId = Integer.toString(organism.getNcbiTaxId());
		String bioSourceUri = IDENTIFIERS_ORG + "taxonomy/" + ncbiId;

		//return if element already exists in model
		BioSource bpBioSource = (BioSource) bpModel.getByID(bioSourceUri);
		if (bpBioSource != null) 
			return bpBioSource;

		// taxon xref
		String taxonXrefUri = xmlBase + "UX_taxonomy_" + ncbiId;
		UnificationXref taxonXref = (UnificationXref) bpModel.getByID(taxonXrefUri);
		if(taxonXref == null) {
			taxonXref = bpModel.addNew(UnificationXref.class, taxonXrefUri);
			taxonXref.setDb("Taxonomy");
			taxonXref.setId(ncbiId);
		}

		// cell type
		CellVocabulary cellType = findOrCreateControlledVocabulary(organism.getCellType(), CellVocabulary.class);

		// tissue
		TissueVocabulary tissue = findOrCreateControlledVocabulary(organism.getTissue(), TissueVocabulary.class);

		String bioSourceName = null;
		if (organism.hasNames()) {
			bioSourceName = getName(organism.getNames());
		}

		return createBioSource(bioSourceUri, taxonXref, cellType, tissue, bioSourceName);
	}


	/*
	 * Given a CvType, return a paxtools ControlledVocabulary.
	 */
	private <T extends ControlledVocabulary> T findOrCreateControlledVocabulary(CvType cvType, Class<T> bpCvClass) {

		if (cvType == null) 
			return null;

		// try full name first, else try short name
		String term = null;
		if (cvType.hasNames()) {
			term = getName(cvType.getNames());
			if (term == null) 
				return null;
		}
		
		String uri = xmlBase + bpCvClass.getSimpleName() + encode(term);
		// look for name in our vocabulary set
		T toReturn = (T) bpModel.getByID(uri);
		if (toReturn != null) 
			return toReturn;

		// create/add a new controlled vocabulary
		Set<Xref> bpXrefs = getXrefs(cvType.getXref(), true);		
		toReturn = bpModel.addNew(bpCvClass, uri);
		toReturn.addTerm(term);
		if (bpXrefs != null && bpXrefs.size() > 0)
			for (Xref bpXref : bpXrefs)
				toReturn.addXref(bpXref);

		return toReturn;
 	}

	/*
	 * Given a psi xref, returns paxtools unification and relationship xrefs.
	 */
	private Set<Xref> getXrefs(psidev.psi.mi.xml.model.Xref psiXREF, boolean cvOrInteraction) {

		// set to return
		Set<Xref> toReturn = new HashSet<Xref>();

		// check args
		if (psiXREF == null) return toReturn;

		// create the list of all psimi xrefs
		List<DbReference> psiDBRefList = new ArrayList<DbReference>();
		psiDBRefList.add(psiXREF.getPrimaryRef());
		if (psiXREF.hasSecondaryRef()) {
			psiDBRefList.addAll(psiXREF.getSecondaryRef());
		}

		for (DbReference psiDBRef : psiDBRefList) {
			if(psiDBRef==null) continue;
			
			// process ref type
			String refType = (psiDBRef.hasRefType()) ? psiDBRef.getRefType() : null;
			String refTypeAc = (psiDBRef.hasRefTypeAc()) ? psiDBRef.getRefTypeAc() : null;
            String psiDBRefId = psiDBRef.getId();

            // If multiple ids given with comma separated values, then split them.
            for (String dbRefId : psiDBRefId.split(",")) {
            	Xref bpXref = null;
                if ("identity".equals(refType) || "identical object".equals(refType)) {
                    bpXref = unificationXref(psiDBRef.getDb(), dbRefId);
                } 
                else if (!cvOrInteraction) {
                	if("secondary-ac".equals(refType) ) {//&& psiDBRef.getDb().toLowerCase().startsWith("uniprot")) {
                		bpXref = unificationXref(psiDBRef.getDb(), dbRefId);
                	} 
                	else {
                		bpXref = relationshipXref(psiDBRef.getDb(), dbRefId, refType, refTypeAc);
                	}
                }

                //set properties for the new xref and add it to the set to return
                if (bpXref != null) {
                    bpXref.setDb(psiDBRef.getDb());
                    bpXref.setId(dbRefId);
                    toReturn.add(bpXref);
                }
            }
        }

		return toReturn;
	}

	
	private Xref unificationXref(String db, String id) {
		String xuri = xmlBase + "UX_" + encode(db.toLowerCase() + "_" + id);
		UnificationXref x = (UnificationXref) bpModel.getByID(xuri);
		if(x==null) {
			x= bpModel.addNew(UnificationXref.class, xuri);
			x.setDb(db);
			x.setId(id);
		}
		return x;
	}


	private Set<PublicationXref> getPublicationXref(psidev.psi.mi.xml.model.Xref psiXREF) {
		Set<PublicationXref> toReturn = new HashSet<PublicationXref>();

		if (psiXREF == null) 
			return toReturn; //empty set

		// get primary 
		DbReference psiDBRef = psiXREF.getPrimaryRef();
		if (psiDBRef == null) 
			return toReturn; //empty set

		// find or create publication xref
		String id = xmlBase + "PX_" + encode(psiDBRef.getDb().toLowerCase() + "_"+ psiDBRef.getId());
		PublicationXref bpXref = (PublicationXref) bpModel.getByID(id);
		if (bpXref == null) {
			bpXref = bpModel.addNew(PublicationXref.class, id);
			bpXref.setDb(psiDBRef.getDb());
			bpXref.setId(psiDBRef.getId());
		}
		
		toReturn.add(bpXref);
		
		return toReturn;
	}


	private String encode(String id) {
		try {
			return URLEncoder.encode(id, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return URLEncoder.encode(id);
		}
	}

	/*
	 * Given an interaction, return a set of paxtools evidence objects.
	 */
	private Set<Evidence> getExperimentalData(Interaction interaction, 
			Map<Participant, SimplePhysicalEntity> psimiParticipantToBiopaxParticipantMap) {

		// set to return
		Set<Evidence> toReturn = new HashSet<Evidence>();

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

				Set<Xref> bpXrefs = new HashSet<Xref>();
				if (experimentDescription.hasXref()) {
					bpXrefs.addAll(getXrefs(experimentDescription.getXref(), false));
				}
				if (experimentDescription.getBibref() != null) {
					bpXrefs.addAll(getPublicationXref(experimentDescription.getBibref().getXref()));
				}
				// host organism list dropped
				
				// confidence list
				Set<Score> scores = new HashSet<Score>();
				if (experimentDescription.hasConfidences()) {
					for (Confidence psiConfidence : experimentDescription.getConfidences()) {
						Score bpScoreOrConfidence = getScoreOrConfidence(psiConfidence);
						if (bpScoreOrConfidence != null) scores.add(bpScoreOrConfidence);
					}
				}
				// attribute list
				if (experimentDescription.hasAttributes()) {
					comments.addAll(getAttributes(experimentDescription.getAttributes()));
				}
				// experimental form
				Set<ExperimentalForm> experimentalForms = getExperimentalFormSet(experimentDescription, interaction,
																			  psimiParticipantToBiopaxParticipantMap);
				
				// interaction detection method, participant detection method, feature detection method
				Set<EvidenceCodeVocabulary> evidenceCodes = getEvidenceCodes(experimentDescription);
				// add evidence to list we are returning
				Evidence evi = createEvidence(bpXrefs, evidenceCodes, scores, comments, experimentalForms);
				toReturn.add(evi);
			}
		}

		return toReturn;
	}

	/*
	 * Given a psi-mi experiment type, returns a set of open
	 * controlled vocabulary objects which represent evidence code(s).
	 */
	private Set<EvidenceCodeVocabulary> getEvidenceCodes(ExperimentDescription experimentDescription) {

		// set to return
		Set<EvidenceCodeVocabulary> toReturn = new HashSet<EvidenceCodeVocabulary>();

		// get experiment methods
		Set<CvType> cvTypeSet = new HashSet<CvType>(3);
		cvTypeSet.add(experimentDescription.getInteractionDetectionMethod());
		cvTypeSet.add(experimentDescription.getParticipantIdentificationMethod());
		cvTypeSet.add(experimentDescription.getFeatureDetectionMethod());

		// create openControlledVocabulary objects for each detection method
		for (CvType cvtype : cvTypeSet) {
			if (cvtype == null) 
				continue;
			EvidenceCodeVocabulary ecv = findOrCreateControlledVocabulary(cvtype, EvidenceCodeVocabulary.class);
			if (ecv != null) 
				toReturn.add(ecv);
		}

		return toReturn;
	}

	/*
	 * Given a psi-mi confidence object, returns a paxtools confidence object.
	 */
	private Score getScoreOrConfidence(Confidence psiConfidence) {

		// check args
		if (psiConfidence == null) 
			return null;

		// psiConfidence.value maps to confidence.confidence-value
		String value = psiConfidence.getValue();

		// get psiConfidence unit
		OpenCvType ocv = psiConfidence.getUnit(); 

		// psiConfidence.unit.xref maps to confidence.xref
		Set<Xref> bpXrefs = new HashSet<Xref>();
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

		Score ret = createScore(value, bpXrefs, comments);
		
		return ret;
	}

	/*
	 * Given a psi-mi attributes list, returns a string set, where
	 * each string is concatenation of name/value pairs.
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

		return toReturn;
	}

	/*
	 * Given a psi-mi interaction element type, returns a set of experimental forms.
	 */
	private Set<ExperimentalForm> getExperimentalFormSet(ExperimentDescription experimentDescription,
													  Interaction interaction,
													  Map<Participant, SimplePhysicalEntity> psimiParticipantToBiopaxParticipantMap) {

		Set<ExperimentalForm> toReturn = new HashSet<ExperimentalForm>();
		
		Set<String> processedRoles = new HashSet<String>();

		// interate through the psi participants, get experimental role
		for (Participant participant : interaction.getParticipants()) {
			// get participant - may be used in following loop
			SimplePhysicalEntity bpParticipant = psimiParticipantToBiopaxParticipantMap.get(participant.getId());
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
								ExperimentalFormVocabulary efv = 
									findOrCreateControlledVocabulary(experimentalRole, ExperimentalFormVocabulary.class);
								toReturn.add(createExperimentalForm(efv, bpParticipant));
								processedRoles.add(roleName);
							}
						}
					}
				}
			}
		}

		return toReturn;
	}

	/*
	 * Given a PSI Names object, returns a name.
	 */
	private String getName(Names name) {		
		if (name.hasFullName()) {
			return name.getFullName();
		}
		else if (name.hasShortLabel()) {
			return name.getShortLabel();
		}

		return null;
	}


	private RelationshipXref relationshipXref(String db, String id, String refType, String refTypeAc)
	{	
		String uri = xmlBase + "RX_" + encode(db.toLowerCase()+"_"+id+"_"+refType);
        RelationshipXref x = (RelationshipXref) bpModel.getByID(uri);                 
        
        if (x == null) { //create/add a new RX
        	x = bpModel.addNew(RelationshipXref.class, uri);
        	if (refType != null) //use the standard CV term and accession
        	{
        		String cvUri = (refTypeAc!=null) ? "http://identifiers.org/psimi/" + refTypeAc
        			: xmlBase + "RTV_" + encode(refType);//the latter should not happen often (ever, in a valid PSI-MI XML)			
        		RelationshipTypeVocabulary rtv = (RelationshipTypeVocabulary) bpModel.getByID(cvUri);
        		if(rtv == null) {
        			rtv = bpModel.addNew(RelationshipTypeVocabulary.class, cvUri);
        			rtv.addTerm(refType);
        			if(refTypeAc != null && !refTypeAc.isEmpty()) {//null happens, e.g., for 'uniprot-removed-ac' terms...
        				UnificationXref cvx = bpModel
        					.addNew(UnificationXref.class, genUri(UnificationXref.class, bpModel));
        				cvx.setDb("PSI-MI");
        				cvx.setId(refTypeAc);
        				rtv.addXref(cvx);
        			}
        		}			
        		x.setRelationshipType(rtv);
        	}		
        }
        
		return x;
	}

	/*
	 * Gets an evidence object.
	 */
	private Evidence createEvidence(Set<? extends Xref> bpXrefs,
	                               Set<EvidenceCodeVocabulary> evidenceCodes,
	                               Set<Score> scores,
	                               Set<String> comments,
	                               Set<ExperimentalForm> experimentalForms)
	{
		Evidence bpEvidence = bpModel.addNew(Evidence.class, genUri(Evidence.class, bpModel));
		if (bpXrefs != null)
		{
			for (Xref bpXref : bpXrefs)
				bpEvidence.addXref((Xref) bpXref);
		}
		if (scores != null && scores.size() > 0)
		{
			for (Score score : scores) {
				bpEvidence.addConfidence((Score)score);
			}
		}
		if (comments != null && comments.size() > 0)
		{
			for (String comment : comments) {
				bpEvidence.addComment(comment.trim());
			}
		}
		if (experimentalForms != null && experimentalForms.size() > 0)
		{
			for (ExperimentalForm experimentalForm : experimentalForms) {
				bpEvidence.addExperimentalForm((ExperimentalForm)experimentalForm);
			}
		}
		
		if(evidenceCodes != null)
			for(EvidenceCodeVocabulary ecv : evidenceCodes)
				bpEvidence.addEvidenceCode(ecv);
			
		return bpEvidence;
	}

	/*
	 * Gets a confidence/score object.
	 */
	private Score createScore(String value, 
			Set<? extends Xref> bpXrefs, Set<String> comments)
	{
		Score bpScore = bpModel.addNew(Score.class, genUri(Score.class, bpModel));
		if (value != null)
		{
			bpScore.setValue(value);
		}
		if (bpXrefs != null && bpXrefs.size() > 0)
		{
			for (Xref xref : bpXrefs) {
				bpScore.addXref((Xref)xref);
			}
		}
		if (comments != null && comments.size() > 0)
		{
			for (String comment : comments) {
				bpScore.addComment(comment);
			}
		}
		return bpScore;
	}

	/*
	 * Gets a experimental form object.
	 */
	private ExperimentalForm createExperimentalForm(ExperimentalFormVocabulary formType, SimplePhysicalEntity participant)
	{
		ExperimentalForm bpExperimentalForm =
				bpModel.addNew(ExperimentalForm.class, genUri(ExperimentalForm.class, bpModel));
		
		if (formType != null) {
			bpExperimentalForm.addExperimentalFormDescription(formType);
		}
		
		if (participant != null) {
			bpExperimentalForm.setExperimentalFormEntity(participant);
		}
		
		return bpExperimentalForm;
	}


	/*
	 * New a molecular interaction.
	 */
	private MolecularInteraction createMolecularInteraction(String name, String shortName,
	                                                  Set<String> availability,
	                                                  Set<? extends SimplePhysicalEntity> participants,
	                                                  Set<Evidence> bpEvidence)
	{
		MolecularInteraction toReturn =
				bpModel.addNew(MolecularInteraction.class, genUri(MolecularInteraction.class, bpModel));
		
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
			for (SimplePhysicalEntity participant : participants) {
				toReturn.addParticipant(participant);
			}
		}
		if (bpEvidence != null && !bpEvidence.isEmpty())
		{
			for (Evidence evidence : bpEvidence) {
				toReturn.addEvidence(evidence);
			}
		}
			
		return toReturn;
	}


	private Complex createComplex(String name, String shortName,
	                              Set<String> availability,
	                              Set<? extends SimplePhysicalEntity> participants,
	                              Set<Evidence> bpEvidence)
	{
		Complex toReturn = bpModel.addNew(Complex.class, genUri(Complex.class, bpModel));
		
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
			for (SimplePhysicalEntity participant : participants) {
				toReturn.addComponent(participant);
			}
		}
		if (bpEvidence != null && !bpEvidence.isEmpty())
		{
			for (Evidence evidence : bpEvidence) {
				toReturn.addEvidence(evidence);
			}
		}
			
		return toReturn;
	}
	

	private BioSource createBioSource(String id, UnificationXref taxonXref,
	                                 CellVocabulary cellType, TissueVocabulary tissue,
	                                 String name)
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
			
		return toReturn;
	}


	private <T extends EntityFeature> T getFeature(Class<T> featureClass, Feature psiFeature)
	{					
		String entityFeatureUri = genUri(featureClass, bpModel); 		
		T entityFeature = (T) bpModel.addNew(featureClass, entityFeatureUri);
		
		// feature location
		Set<SequenceInterval> featureLocations = getSequenceLocation(psiFeature.getRanges());
		if (featureLocations != null)
			for (SequenceLocation featureLocation : featureLocations) 
				entityFeature.setFeatureLocation(featureLocation);
		
		// feature type
		//TODO why always SequenceRegionVocabulary?
		if (psiFeature.hasFeatureType()) {
			SequenceRegionVocabulary cv = findOrCreateControlledVocabulary(
					psiFeature.getFeatureType(), SequenceRegionVocabulary.class);
			entityFeature.setFeatureLocationType(cv);
		}
			
		//TODO set bindsTo for BindingFeatures if possible (using <interaction><inferredInteractionList>.. psimi elements/attr.)
		
		return entityFeature;
	}

	
	private SequenceInterval getSequenceLocation(long beginSequenceInterval,
	                                            long endSequenceInterval)
	{
			SequenceInterval toReturn =
					bpModel.addNew(SequenceInterval.class, genUri(SequenceInterval.class, bpModel));
			SequenceSite bpSequenceSiteBegin =
					bpModel.addNew(SequenceSite.class, genUri(SequenceSite.class, bpModel));
			bpSequenceSiteBegin.setSequencePosition((int) beginSequenceInterval);
			toReturn.setSequenceIntervalBegin(bpSequenceSiteBegin);
			SequenceSite bpSequenceSiteEnd =
					bpModel.addNew(SequenceSite.class, genUri(SequenceSite.class, bpModel));
			bpSequenceSiteEnd.setSequencePosition((int) endSequenceInterval);
			toReturn.setSequenceIntervalEnd(bpSequenceSiteEnd);
			
			return toReturn;
	}
	

	/*
	 * Given a set of evidence objects, determines if interaction (that evidence obj is derived from)
	 * is a genetic interaction.
	 */
	private boolean isGeneticInteraction(final List<String> geneticInteractionTerms,
	                                    Set<Evidence> bpEvidence)
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
		return false;
	}

	
	/*
	 * Generates a URI of a BioPAX object using the xml base, model interface name 
	 * and generated number (sequential).
	 * The idea is virtually never ever return the same URI here (taking into account 
	 * that there are multiple threads converting different PSIMI Entries, one per thread, 
	 * symultaneously)
	 */
	private String genUri(Class<? extends BioPAXElement> type, Model model) {
		return xmlBase + type.getSimpleName() + "_" + counter.incrementAndGet();
	}	
}
