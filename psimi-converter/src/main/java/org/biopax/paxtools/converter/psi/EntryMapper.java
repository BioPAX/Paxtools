package org.biopax.paxtools.converter.psi;

import java.util.*;

import org.apache.commons.lang3.ArrayUtils;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
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
import org.biopax.paxtools.model.level3.Gene;
import org.biopax.paxtools.model.level3.GeneticInteraction;
import org.biopax.paxtools.model.level3.InteractionVocabulary;
import org.biopax.paxtools.model.level3.ModificationFeature;
import org.biopax.paxtools.model.level3.MolecularInteraction;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.model.level3.Provenance;
import org.biopax.paxtools.model.level3.PublicationXref;
import org.biopax.paxtools.model.level3.RelationshipTypeVocabulary;
import org.biopax.paxtools.model.level3.RelationshipXref;
import org.biopax.paxtools.model.level3.Rna;
import org.biopax.paxtools.model.level3.RnaReference;
import org.biopax.paxtools.model.level3.Score;
import org.biopax.paxtools.model.level3.SequenceEntityReference;
import org.biopax.paxtools.model.level3.SequenceInterval;
import org.biopax.paxtools.model.level3.SequenceLocation;
import org.biopax.paxtools.model.level3.SequenceModificationVocabulary;
import org.biopax.paxtools.model.level3.SequenceRegionVocabulary;
import org.biopax.paxtools.model.level3.SequenceSite;
import org.biopax.paxtools.model.level3.SimplePhysicalEntity;
import org.biopax.paxtools.model.level3.SmallMolecule;
import org.biopax.paxtools.model.level3.SmallMoleculeReference;
import org.biopax.paxtools.model.level3.TissueVocabulary;
import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.model.level3.Xref;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import psidev.psi.mi.xml.model.*;


/**
 * PSIMI 'entry' to BioPAX converter. 
 * 
 * PS (rodche, IR):
 * The PSI-MI api is weird. It has methods/classes that pretend to follow the XML schema (OXM like)
 * but in fact some of those do not make any practical sense, because they are either cleared or reset by the 
 * PSI-MI parser (post-processing); e.g., look at (try) all those get*Ref or has*Ref, etc. methods.
 * PSI-MI specification itself is tricky (e.g., the ability to define/express multiple experiment 
 * descriptions per interaction, multiple roles per participant, multiple experimentalInteractors 
 * per participant of an interaction are probably altogether unnecessary) and uses conventions 
 * and practices not formally defined in the schema (e.g., how/whether to use "id" attribute, 
 * absence of some xml elements means sometimes ALL such elements from the parent element (see e.g. experimentRef).
 *
 * @author Benjamin Gross, rodche (major re-factoring for Level3; fixing, adding genetic interactions, experimental form entities and features, etc.)
 */
class EntryMapper {

	private static final Logger LOG = LoggerFactory.getLogger(EntryMapper.class);
	
	private static final ArrayList<String> GENETIC_INTERACTIONS;
	
	static {
		GENETIC_INTERACTIONS = new ArrayList<>();
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
	public static final String BIOGRID_EVIDENCE_CODE = "BioGRID Evidence Code";
	public static final String EXPERIMENTAL_FORM_ENTITY_COMMENT = "experimental form entity";

	// IntAct annotation keys
	public static final String FIGURE_LEGEND_CODE = "figure legend";
	
	private final Model bpModel;
	
	private final String xmlBase;
	
	private long counter;
	
	private final boolean forceInteractionToComplex;
	
	/**
	 * Constructor.
	 *
	 * @param model
	 * @param forceInteractionToComplex - always generate Complex instead of MolecularInteraction
	 */
	public EntryMapper(Model model, boolean forceInteractionToComplex) {
		this.bpModel = model;
		this.xmlBase = (model.getXmlBase()==null) ? "" : model.getXmlBase();
		this.counter = System.currentTimeMillis();
		this.forceInteractionToComplex = forceInteractionToComplex;
	}


	/**
	 * Convert a PSIMI entry to BioPAX
	 * interactions, participants, etc. objects 
	 * and add to the target BioPAX model.
	 * 
	 * @param entry
	 */
	public void run(Entry entry) {	
		
		// get availabilities
		final Set<String> avail = new HashSet<>();
		if(entry.hasAvailabilities()) {
			for (Availability a : entry.getAvailabilities())
				if (a.hasValue()) 
					avail.add(a.getValue());	
		}
		
		// get data source
		final Provenance pro = createProvenance(entry.getSource());
		
		//build a skip-set of "interactions" linked by participant.interactionRef element;
		//we'll then create Complex type participants from these in-participant interactions.
		Set<Interaction> participantInteractions = new HashSet<>();
		for(Interaction interaction : entry.getInteractions()) {
			for(Participant participant : interaction.getParticipants()) {
				//checking for hasInteraction()==true only is sufficient; 
				//i.e., we ignore hasInteractionRef(), getInteractionRefs(), 
				//because these in fact get cleared by the PSI-MI parser:
				if(participant.hasInteraction()) {
					participantInteractions.add(participant.getInteraction());
				}
			}
		}
		
		// iterate through the "root" psimi interactions only and create biopax interactions or complexes
		for (Interaction interaction : entry.getInteractions()) {
			if(!participantInteractions.contains(interaction)) {
				// TODO future (hard): make a Complex or Interaction based on the interaction type ('direct interaction' or 'physical association' (IntAct) -> complex)
				processInteraction(interaction, avail, pro, false);
			}
		}
	}
	

	private Provenance createProvenance(Source source) {
		Provenance pro = null;		
		String name = null;
		PublicationXref px = null;
		UnificationXref ux = null;
		
		if (source.hasNames()) {
			name = getName(source.getNames());
		} 
		
		if(source.hasXref()) {
			ux = getPrimaryUnificationXref(source.getXref());
			if(name==null && ux != null)
				name = ux.getDb()+"_"+ux.getId();
		} 
			
		if(source.hasBibref()) {
			psidev.psi.mi.xml.model.Xref psiXREF = source.getBibref().getXref();
			if (psiXREF != null) {
				px = publicationXref(psiXREF.getPrimaryRef().getDb(), psiXREF.getPrimaryRef().getId());
			}
			
			if(name==null && px != null)
				name = px.getDb()+"_"+px.getId();
		}
		
		String ver = null;
		if(source.hasReleaseDate()) {
			ver = source.getRelease();
			if(name==null)
				name = ver;
		}
		
		String sourceUri = (name!=null) 
			? xmlBase + "Provenance_" + encode(name)
				: genUri(Provenance.class);
		
		//unless it's already there,
		pro = (Provenance) bpModel.getByID(sourceUri);		
		if(pro == null) { //generate a new one
			pro = bpModel.addNew(Provenance.class, sourceUri);
			
			if(name != null)
				pro.setDisplayName(name);
			
			if(px != null)
				pro.addXref(px);
			
			if(ux != null)
				pro.addXref(ux);
			
			if(source.hasAttributes())
				for(Attribute attr : source.getAttributes())
					pro.addComment(attr.toString());
			
			if(ver != null)
				pro.addComment("Release Date: " + ver);
		}
		
		return pro;
	}

	
	/*
	 * Creates a paxtools object that
	 * corresponds to the psi interaction.
	 *
	 * Note:
	 * psi.interactionElementType                 -&gt; biopax Complex, MolecularInteraction, or GeneticInteraction
	 * psi.interactionElementType.participantList -&gt; biopax interaction/complex participants/components
	 */
	private Entity processInteraction(Interaction interaction, Set<String> avail, 
			Provenance pro, boolean isComplex) 
	{	
		Entity bpInteraction = null; //interaction or complex
		boolean isGeneticInteraction = false;
		
		// get interaction name/short name
		String name = null;
		String shortName = null;
		if (interaction.hasNames()) {
			Names names = interaction.getNames();
			name = (names.hasFullName()) ? names.getFullName() : "";
			shortName = (names.hasShortLabel()) ? names.getShortLabel() : "";
		}
		
		final Set<InteractionVocabulary> interactionVocabularies = new HashSet<>();
		if (interaction.hasInteractionTypes()) {
			for(CvType interactionType : interaction.getInteractionTypes()) {
				//generate InteractionVocabulary and set interactionType
				InteractionVocabulary cv = findOrCreateControlledVocabulary(interactionType, InteractionVocabulary.class);
				if(cv != null)
					interactionVocabularies.add(cv);
			}
		}		
		
		// using experiment descriptions, create Evidence objects 
		// (yet, no experimental forms/roles/entities are created here)
		Set<Evidence> bpEvidences = new HashSet<>();
		if (interaction.hasExperiments()) {			
			bpEvidences = createBiopaxEvidences(interaction);
		}
		
		//A hack for e.g. IntAct or BIND "gene-protein" interactions (ChIp and EMSA experiments)
		// where the interactor type should probably not be 'gene' (but 'dna' or 'rna')
		Set<String> participantTypes = new HashSet<>();
		for(Participant p : interaction.getParticipants()) {
			if(p.hasInteractor()) {
				String type = getName(p.getInteractor().getInteractorType().getNames());
				if(type==null) type = "protein"; //default type (if unspecified)
				participantTypes.add(type.toLowerCase());
			} else if (p.hasInteraction()) {
				participantTypes.add("complex"); //hierarchical complex build up
			} // else? (impossible!)
		}
		// If there are both genes and physical entities present, let's 
		// replace 'gene' with 'dna' (esp. true for "ch-ip", "emsa" experiments);
		// (this won't affect experimental form entities if experimentalInteractor element exists)
		if(participantTypes.size() > 1 && participantTypes.contains("gene")) {
			//TODO a better criteria to reliably detect whether 'gene' interactor type actually means Dna/DnaRegion or Rna/RnaRegion, or indeed Gene)
			LOG.warn("Interaction: " + interaction.getId() + ", name(s): " + shortName + " " + name 
					+ "; has both 'gene' and physical entity type participants: " + participantTypes 
					+ "; so we'll replace 'gene' with 'dna' (a quick fix)");
			for(Participant p : interaction.getParticipants()) {
				if(p.hasInteractor() && p.getInteractor().getInteractorType().hasNames()) {
					String type = getName(p.getInteractor().getInteractorType().getNames());
					if("gene".equalsIgnoreCase(type)) {
						p.getInteractor().getInteractorType().getNames().setShortLabel("dna");
					}
				}
			}	
		}		
		
		// interate through the psi-mi participants, create corresp. biopax entities
		final Set<Entity> bpParticipants = new HashSet<>();
		for (Participant participant : interaction.getParticipants()) {
			// get paxtools physical entity participant and add to participant list
			// (this also adds experimental evidence and forms)
			Entity bpParticipant = createBiopaxEntity(participant, avail, pro); 
			if (bpParticipant != null) {
				if(!bpParticipants.contains(bpParticipant))
					bpParticipants.add(bpParticipant);
			}
		}
				
		// Process interaction attributes.
		final Set<String> comments = new HashSet<>();
		// Set GeneticInteraction flag.
		// As of BioGRID v3.1.72 (at least), genetic interaction code can reside
		// as an attribute of the Interaction via "BioGRID Evidence Code" key
		if (interaction.hasAttributes()) {
			for (Attribute attribute : interaction.getAttributes()) {
				String key = attribute.getName(); //may be reset below
				String value = (attribute.hasValue()) ? attribute.getValue() : "";
				if(key.equalsIgnoreCase(BIOGRID_EVIDENCE_CODE)
						&& GENETIC_INTERACTIONS.contains(value))
				{
					isGeneticInteraction = true; // important!
				}
				comments.add(key + ":" + value);
			}
		}
		// or, if all participants are 'gene' type, make a biopax GeneticInteraction
		if(participantTypes.size() == 1 && participantTypes.contains("gene")) {
			isGeneticInteraction = true;
		}		
		//or, check another genetic interaction flag (criteria)
		if(!isGeneticInteraction) {
			isGeneticInteraction = isGeneticInteraction(bpEvidences);
		}
		
		if ((isComplex || forceInteractionToComplex) && !isGeneticInteraction) {
			bpInteraction = createComplex(bpParticipants, interaction.getImexId(), interaction.getId());
		} else if(isGeneticInteraction) {
			bpInteraction = createGeneticInteraction(bpParticipants, interactionVocabularies,
					interaction.getImexId(), interaction.getId()
			);
		} else {
			bpInteraction = createMolecularInteraction(bpParticipants, interactionVocabularies,
					interaction.getImexId(), interaction.getId());
		}

		for(String c : comments) {
			bpInteraction.addComment(c);
		}

		//add evidences to the interaction/complex bpEntity
		for (Evidence evidence : bpEvidences) {
			bpInteraction.addEvidence(evidence);
			//TODO: shall we add IntAct "figure legend" comment to the evidences as well?
		}
		
		addAvailabilityAndProvenance(bpInteraction, avail, pro);
		
		if (name != null)
			bpInteraction.addName(name);
		if (shortName != null) {
			if(shortName.length()<51)
				bpInteraction.setDisplayName(shortName);
			else
				bpInteraction.addName(shortName);
		}
				
		// add xrefs		
		Set<Xref> bpXrefs = new HashSet<>();		
		if (interaction.hasXref()) {
			bpXrefs.addAll(getXrefs(interaction.getXref()));
		}

		for (Xref bpXref : bpXrefs) {
			bpInteraction.addXref(bpXref);
		}
		
		return bpInteraction;
	}

	
	private Set<Evidence> createBiopaxEvidences(Interaction interaction) {
		Set<Evidence> evidences = new HashSet<>();
		
		for (ExperimentDescription experimentDescription : interaction.getExperiments()) {
			// build and add evidence
			String evUri = genUri(Evidence.class,
					interaction.getImexId(), interaction.getId(), experimentDescription.getId());
			
			Evidence evidence = bpModel.addNew(Evidence.class, evUri);

			if (experimentDescription.hasXref()) {
				for(Xref xref : getXrefs(experimentDescription.getXref()))
					evidence.addXref(xref);
			}
			if (experimentDescription.getBibref() != null) {
				psidev.psi.mi.xml.model.Xref psiXREF = experimentDescription.getBibref().getXref();
				if (psiXREF != null) {
					PublicationXref px = publicationXref(psiXREF.getPrimaryRef().getDb(), psiXREF.getPrimaryRef().getId());
					if(px != null) 
						evidence.addXref(px);
				}
			}
					
			// create comments
			// from names (there is no biopax Evidence.name property)
			if (experimentDescription.hasNames()) {
				Names names = experimentDescription.getNames();
				if(names.hasFullName())
					evidence.addComment(names.getFullName().trim());
				if(names.hasShortLabel())
					evidence.addComment(names.getShortLabel().trim());
			}
			
			// from attributes
			if (experimentDescription.hasAttributes()) {
				for(String attr : getAttributes(experimentDescription.getAttributes()))
					evidence.addComment(attr.trim());
			}

			//add hostOrganism info to comments (there is no 'organism' property of Evidence, EF, etc)
			if(experimentDescription.hasHostOrganisms()) {
				for(Organism organism : experimentDescription.getHostOrganisms())
					evidence.addComment("Host " + organism.toString());
			}

			// confidence list
			if (experimentDescription.hasConfidences()) {
				for (Confidence psiConfidence : experimentDescription.getConfidences()) {
					Score bpScoreOrConfidence = getScoreOrConfidence(psiConfidence);
					if (bpScoreOrConfidence != null) 
						evidence.addConfidence(bpScoreOrConfidence);
				}
			}		
			
			// interaction detection method, participant detection method, feature detection method
			Set<EvidenceCodeVocabulary> evidenceCodes = getEvidenceCodes(experimentDescription);			
			if(evidenceCodes != null)
				for(EvidenceCodeVocabulary ecv : evidenceCodes)
					evidence.addEvidenceCode(ecv);
			
			evidences.add(evidence);
		}
		
		return evidences;
	}


	private void addAvailabilityAndProvenance(Entity bpEntity,
			Set<String> avail, Provenance pro) {
		if(pro != null)
			bpEntity.addDataSource(pro);
		
		if(avail != null)
			for(String a : avail)
				bpEntity.addAvailability(a);
	}


	/*
	 * Converts a PSI-MI interaction's participant 
	 * to a BioPAX physical entity or gene
	 * and corresponding experimental form entities.
	 */
	private Entity createBiopaxEntity(Participant participant, Set<String> avail, Provenance pro) 
	{	
		LOG.debug("createBiopaxEntity, processing participant: " + participant.getId());
		
		// create a new PE or Gene to be
		Entity entity = null;
	
		// The PSI-MI parser does not set 'interactorRef' 
		// (or it rather clears that property and updates 'interactor' prop. instead;  
		// see junit tests).
		if (participant.hasInteractor()) {
			//participant.getInteractor() is not null
			entity = createParticipant(participant, participant.getInteractor(), avail, pro, false);
		} else if(participant.hasInteraction()) {
			//hierarchical buildup of a complex (participant.hasInteraction==true)...
			entity = (Complex) processInteraction(participant.getInteraction(), avail, pro, true);
		}
				
		if(entity == null) 
			return null;
		
		//create new Evidence for this participant
		Evidence participantEvidence = bpModel.getLevel().getDefaultFactory()
			.create(Evidence.class, genUri(Evidence.class, "p",participant.getId()));
		
		if(participant.hasExperimentalRoles() 
			|| participant.hasExperimentalInteractors() 
				|| participant.hasFeatures()) 
		{
			if(participant.hasExperimentalInteractors()) {
				//create and collect experimental entities for current participant:
				for(ExperimentalInteractor experimentalInteractor : participant.getExperimentalInteractors()) {
					Interactor interactor = experimentalInteractor.getInteractor();
					LOG.debug("createBiopaxEntity, participant: " + participant.getId() 
							+ ", exp. interactor: " + interactor.getId());
					Entity expEntity = createParticipant(participant, interactor, avail, pro, true);
					assert expEntity!=null : "expEntity is null";
					
					//try to reuse existing experimental form entity
					expEntity = findEquivalentEntity(expEntity);
					if(!bpModel.containsID(expEntity.getUri())) {
						bpModel.add(expEntity);
					}
					
					//workaround a PSI-MI parser issue (no exp. or exp.refs means to apply to all interaction's experiments):
					if(experimentalInteractor.hasExperiments()) {
						for(ExperimentDescription exp : experimentalInteractor.getExperiments()) {
							LOG.debug("createBiopaxEntity, making EFs for exp: " 
									+ exp.getId() + "; exp.ent: " + expEntity.getUri());
							createAddExperimentalForm(participant, participantEvidence, expEntity, exp);
							
						}
					} else {
						LOG.debug("createBiopaxEntity, making a EForm - one for all experiments " 
								+ "; exp.ent: " + expEntity.getUri());
						createAddExperimentalForm(participant, participantEvidence, expEntity, null);
					}
				}		
			} else {
				LOG.debug("createBiopaxEntity, participant: " + participant.getId() 
						+ " doesn't have any exp. interactors");
				createAddExperimentalForm(participant, participantEvidence, null, null);
			}
			
			//save the participant entity evidence if it's not empty (roles, forms were actually generated)
			if(!participantEvidence.getExperimentalForm().isEmpty()) {
				bpModel.add(participantEvidence);
				entity.addEvidence(participantEvidence);
				//do not call findEquivalentEntity(entity) for this one having not empty evidence
			} else {
				//try to merge/reuse existing equivalent entity
				entity = findEquivalentEntity(entity);
			}
		}
	
		//finally, add, if new, entity to the model and return
		if(!bpModel.containsID(entity.getUri())) {
			bpModel.add(entity);
		}
		
		return entity;
	}
	
	
	/*
	 * Carefully find an existing equivalent physical entity or gene  
	 * or return the same one unchanged.
	 */
	private Entity findEquivalentEntity(final Entity entity) {
		Entity toReturn = entity;
		
		Class<? extends Entity> entityClass = PhysicalEntity.class;
		if(entity instanceof Gene)
			entityClass = Gene.class;
		
		for(Entity existingEntity : bpModel.getObjects(entityClass)) {
			// replace with an existing equivalent entity iif
			if( (//both are not experimental form entities
				 !entity.getComment().contains(EXPERIMENTAL_FORM_ENTITY_COMMENT)
				 && !existingEntity.getComment().contains(EXPERIMENTAL_FORM_ENTITY_COMMENT)
				)
				|| 
				(//both are experimental form entities
				 entity.getComment().contains(EXPERIMENTAL_FORM_ENTITY_COMMENT)
				 && existingEntity.getComment().contains(EXPERIMENTAL_FORM_ENTITY_COMMENT)
				 //and if disp.names match: names like 'GST-Max' are often used to describe states, instead of using other psi-mi features...
				 && entity.getDisplayName().equalsIgnoreCase(existingEntity.getDisplayName())
				)
			){	
				//and if there are no evidences yet, and the two are equivalent, then return existing one (to replace the entity)
				if(existingEntity.getEvidence().isEmpty() && entity.getEvidence().isEmpty() 
						&& existingEntity.isEquivalent(entity)) {
					toReturn = (Entity) existingEntity;
					break;
				}
			}
		}
		
		return toReturn;
	}


	/*
	 * Converts a PSIMI participant to BioPAX physical entity or gene. 
	 * It can be then used either as a participant of an interaction 
	 * or exp. form entity of a exp. form of a participant's evidence,
	 * depending on the caller method and the map provided.
	 * The result entity is not added to the model yet (but its entity reference, 
	 * if applicable, Xrefs, CVs are added to the model).
	 * 
	 * @param participant - of a PSI-MI interaction
	 * @param interactor - participant's interactor or experimental interactor's interactor
	 * @param avail - (biopax) availability
	 * @param pro - (biopax) provenance
	 * @param isExperimentalForm - a flag that controls entity's key comments (important)  
	 * 							 and how entity references are merged (e.g., copy names or not)
	 */
	private Entity createParticipant(Participant participant, Interactor interactor, 
			Set<String> avail, Provenance pro, boolean isExperimentalForm) 
	{
		if(interactor==null)
			throw new AssertionError("createParticipant: participant: " 
					+ participant.getId() + " has got null interactor");
		
		// Find or create the physical entity and entity reference -		
		// figure out physical entity type (protein, dna, rna, small molecule, gene)
		String entityType = null;
		CvType interactorType = interactor.getInteractorType();
		if (interactorType != null && interactorType.hasNames()) {
			entityType = getName(interactorType.getNames());
		}

		// get names/synonyms from the psimi interactor (participant does not have them)
		String name = null;
		String shortName = null;
		Set<String> synonyms = new HashSet<>();
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
		
		//get cellular location, if any
		CellularLocationVocabulary cellularLocation = null;
		if((interactor.hasOrganism() && interactor.getOrganism().hasCompartment())) {
			cellularLocation = findOrCreateControlledVocabulary(
					interactor.getOrganism().getCompartment(), CellularLocationVocabulary.class);
		}
		
		// make one entity reference (ER) per unique interactor (merge duplicate/equivalent ones)
		// make one physical entity (PE) per unique participant... 		
		// (a unique state PE is defined by the ER and cell. location, but not by entity features - which are to go with ExperimentalForms)		
		Class<? extends Entity> entityClass = Protein.class; //default
		Class<? extends EntityReference> entityReferenceClass = ProteinReference.class; //default
		if(entityType!=null && !entityType.isEmpty()) {
			//entityType should be a term from MI:0313 "interactor type" CV
			entityType = entityType.toLowerCase();
			if ("small molecule".equals(entityType)
					|| "polysaccharide".equals(entityType)
					|| "bioactive entity".equals(entityType))
			{
				entityClass = SmallMolecule.class;
				entityReferenceClass = SmallMoleculeReference.class;
			} else if ("dna".equals(entityType) 
					|| "deoxyribonucleic acid".equals(entityType) 
					|| entityType.contains("deoxyribonucleic")
					|| entityType.contains("dna")
					|| "nucleic acid".equals(entityType)) //when can't tell dna from rna? guess DNA
			{
				entityClass = Dna.class;
				entityReferenceClass = DnaReference.class;
			} else if ("rna".equals(entityType)
					|| "ribonucleic acid".equals(entityType)
					|| "poly adenine".equals(entityType)
					|| entityType.contains("rna"))
			{
				entityClass = Rna.class;
				entityReferenceClass = RnaReference.class;
			} else if ("gene".equals(entityType))
			{ //NOTE: Gene "gene" type is for Genetic Interactions only (not transcription/translation regulation, etc.), but...
				entityClass = Gene.class;
				entityReferenceClass = null;
			} else if ("complex".equals(entityType) || entityType.contains("complex"))
			{
				entityClass = Complex.class;
				entityReferenceClass = null;
			} else if ("interaction".equals(entityType))
			{
//				entityClass = null;
//				entityReferenceClass = null;
				LOG.warn("EntryMapper.createParticipant(): skip for interactor: " + interactor.getId()
						+ " that has type: " + entityType 
						+ ", participant:" + participant.getId() + ").");
				return null;
			}
						
			//else - peptide, biopolymer, molecule set, unknown, etc. - consider this is a Protein.
		}
		
		//make consistent biopax URI (for the entity ref., if applies, or phys. entity)
		String entityUri = "";
		String baseUri = "";
		
		final RelationshipXref x = getInteractorPrimaryRef(interactor.getXref());
		if(x != null) {
			baseUri += encode(x.getDb() + "_" + x.getId());
			if(x.getRelationshipType()!=null)
				baseUri += "_" + encode(x.getRelationshipType().getTerm().iterator().next());
			entityUri = baseUri + "_" + String.valueOf(counter++);
		} else { //when no xrefs present, use a number ending (always increment);
			baseUri = String.valueOf(counter++); //new unique part (seldom happens, when no primary xref...)
			entityUri = baseUri;
		}
		
		if(cellularLocation != null)
			entityUri += "_" + encode(cellularLocation.getTerm().iterator().next());
		
		//makes URI look like xmlBase+type+unique_suffix...
		entityUri = xmlBase + entityClass.getSimpleName() + "_" + entityUri;	
		if(entityReferenceClass != null)
			baseUri = xmlBase + entityReferenceClass.getSimpleName() + "_" + baseUri;	
		
		// create a new PE or Gene (don't add to the model yet, for there may exist an equivalent one)
		Entity entity = bpModel.getLevel().getDefaultFactory().create(entityClass, entityUri);
		
		addAvailabilityAndProvenance(entity, avail, pro);
		//and names
		if (name != null) {
			//quite a few PSIMI providers use too long text (like comments) for <fullName> fields...
			if(name.length() > 100 && shortName != null) 
				entity.addComment(name);
			else 
				entity.addName(name);
		}
		if (shortName != null) {
            entity.setDisplayName(shortName);
		}				
		// set cellular location if possible
		if(cellularLocation != null && entity instanceof PhysicalEntity)
			((PhysicalEntity)entity).setCellularLocation(cellularLocation);
		
		final Set<Xref> bpXrefsOfInteractor = getXrefs(interactor.getXref());
		final BioSource bioSource = getBioSource(interactor);
		
		// when not a Gene, we are to find/generate corresponding EntityRererence;
		// but we do not want to gererate apparently duplicate objects...
		if(entityReferenceClass != null) {
			EntityReference entityReference = null;
			
			/* check if the entity ref. with this URI already exists 
			 * (we'll the use that instead of generating a new one every time;
			 * unfortunately, chances still are that we generate same URI 
			 * from non-equivalent psi-mi interactors, though this would probably 
			 * flag for semantic errors in the psi-mi data, such as using same xrefs,
			 * features, etc. for different xml entries unnecessarily).
			 * Unfortunately, we cannot simply create a new ER and compare
			 * with an existing one using Paxtools (as of paxtools-core 4.x and older, 
			 * two ERs are NOT equivalent unless they either have the same URI, or - same organism and sequence).
			 */
			EntityReference er = (EntityReference) bpModel.getByID(baseUri);			
			if( er != null 
				&& er.getModelInterface()==entityReferenceClass 
				&& (!(er instanceof SequenceEntityReference) 
					|| sameNameOrUndefined(((SequenceEntityReference)er).getOrganism(),bioSource)
					)
			) {
				
				entityReference = er; // ok to reuse
				
			} else if(er != null) {
				String newUri = baseUri + "_" + (counter++);
				LOG.warn("A different " + er.getModelInterface().getSimpleName()
					+ ", URI=" + baseUri + ", was found; for interactor:" + interactor.getId()
					+ ", a new ("+entityReferenceClass.getSimpleName()+") URI will be used:" + newUri);
				baseUri = newUri;
			}
			
			//if not found above, create a new ER
			if(entityReference == null) { 
				entityReference = bpModel.addNew(entityReferenceClass, baseUri);	
				
				if (shortName != null) {
					entityReference.setDisplayName(shortName);
				}
				
				//set organism if it's not a small molecule
				if(entityReference instanceof SequenceEntityReference) {
					SequenceEntityReference ser = (SequenceEntityReference)entityReference;
					ser.setOrganism(bioSource);
					ser.setSequence(interactor.getSequence());
				}
			}
				
			//update the displayName if we've got to reuse the existing ER,
			//and this is not an experimental interactor, and new name is shorter
			if(entityReference == er && shortName != null && !isExperimentalForm) {
				if(er.getDisplayName() != null) {
					if(shortName.length() < er.getDisplayName().length()) {
						entityReference.addName(er.getDisplayName()); //keep both names
						entityReference.setDisplayName(shortName);
					} else {
						entityReference.addName(shortName); //keep both names
					}
				} else {
					entityReference.setDisplayName(shortName);
				}
			}
			
			// if we're not reusing existing ER, and this interactor is not experimental,
			// copy names and xrefs to the exisiting ER
			if( !(isExperimentalForm && entityReference == er) ) {
				if (name != null) {
					if(name.length() > 100 && //and there are other names
							(entityReference.getDisplayName()!=null || (synonyms!=null && !synonyms.isEmpty())))
						entityReference.addComment(name); //comment instead of 'standardName'
					else		
						entityReference.addName(name);
				}
				if (synonyms != null) {
					for (String synonym : synonyms)
						entityReference.addName(synonym);
				}
				if (bpXrefsOfInteractor != null) {
					for (Xref xref : bpXrefsOfInteractor)
						entityReference.addXref((Xref) xref);
				}
			}
			
			// set ER
			((SimplePhysicalEntity)entity).setEntityReference(entityReference);		
			
		} else { //i.e., entity is a Gene or Complex
			//add gene's other names, xrefs, organism (for non-genes, these're added to the ER)
			if (synonyms != null) {
				for (String synonym : synonyms) {
					entity.addName(synonym);
				}
			}		
			if (bpXrefsOfInteractor != null) {
				for (Xref xref : bpXrefsOfInteractor)
					entity.addXref((Xref) xref);
			}
			if(entity instanceof Gene) //can be Complex here as well
				((Gene)entity).setOrganism(bioSource);
		}
		
		//Merge some of equivalent entities to avoid unnecessary duplicates 
		// CAREFULLY, e.g.:
		// - never merge a participant entity with an experimental form entity,
		// - never merge if one entity have evidence, etc.
		//(equiv. ERs are being merged regardless this)
		//Merging entities (states) can mess up names, features, exp. roles!
		if(isExperimentalForm)
			entity.addComment(EXPERIMENTAL_FORM_ENTITY_COMMENT);
		else
			entity.addComment("psi-mi participant");
		
		return entity;
	}

	
	/*
	 * True if both organisms are null
	 * or have null (both) or equal (ignoring case)
	 * organism names; false otherwise.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private boolean sameNameOrUndefined(BioSource a, BioSource b) {
		return (a==null)? b==null 
			: 
			( (b==null)? false 
				: //when both a and b are not null, check if names are same
				a.getDisplayName()==null && b.getDisplayName()==null
				||
				a.getDisplayName()!=null && a.getDisplayName().equalsIgnoreCase(b.getDisplayName())
			);
	}

	
	/*
	 * Given a psiFeature, return the set
	 * of SequenceInterval (sequence locations). 
	 */
	private Set<SequenceInterval> getSequenceLocation(Collection<Range> rangeList) {
		Set<SequenceInterval> toReturn = new HashSet<>();

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
	private BioSource getBioSource(Interactor interactor) {
		
		Organism organism = interactor.getOrganism();
		
		// trivial check
		if (organism == null) 
			return null;

		// cell type (can be undefined, i.e., null)
		CellVocabulary cellType = findOrCreateControlledVocabulary(organism.getCellType(), CellVocabulary.class);
		// tissue (can be null)
		TissueVocabulary tissue = findOrCreateControlledVocabulary(organism.getTissue(), TissueVocabulary.class);

		// set the BioPAXElement URI and taxonomy xref id
		String ncbiId = Integer.toString(organism.getNcbiTaxId());
		String name = null;
		
		if (organism.hasNames()) {
			name = getName(organism.getNames());
			//a hack for BIND data (and perhaps other DBs)
			if(name != null && ("homo sapiens".equalsIgnoreCase(name)
					|| "human".equalsIgnoreCase(name))
					&& !"9606".equals(ncbiId)) {
				LOG.error("Taxonomy: " 
					+ ncbiId + " of organism: " + organism 
					+ ", interactor: " + interactor.getId() 
					+ " does not belong to '" 
					+ name + "'; the name will be removed. "
					+ "Data provider should probably use <experimentalInteractor> in addition to <interactor>");
				name = null;
			}
		}
		
		String uri = xmlBase + "BioSource_" + 
			"taxonomy_" + ncbiId; //tissue and cell type terms can be added below		
		if(tissue!=null && !tissue.getTerm().isEmpty()) 
			uri += "_" + encode(tissue.getTerm().iterator().next());
		if(cellType!=null && !cellType.getTerm().isEmpty()) 
			uri += "_" + encode(cellType.getTerm().iterator().next());

		//return if element already exists in model
		BioSource toReturn = (BioSource) bpModel.getByID(uri);
		if (toReturn != null) 
			return toReturn;
		
		toReturn = bpModel.addNew(BioSource.class, uri);
		
		String taxonXrefUri = xmlBase + "UnificationXref_taxonomy_" + ncbiId;
		UnificationXref taxonXref = (UnificationXref) bpModel.getByID(taxonXrefUri);
		if(taxonXref == null) {
			taxonXref = bpModel.addNew(UnificationXref.class, taxonXrefUri);
			taxonXref.setDb("Taxonomy");
			taxonXref.setId(ncbiId);
		}
		toReturn.addXref((Xref)taxonXref);
		
		if (cellType != null)
		{
			toReturn.setCellType((CellVocabulary) cellType);
		}
		if (tissue != null)
		{
			toReturn.setTissue((TissueVocabulary) tissue);
		}
		if (name != null) {
			toReturn.setStandardName(name);
		}
				
		return toReturn;
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
		}

		//xref and primaryRef must always exist, acc. to the schema
		UnificationXref bpXref = getPrimaryUnificationXref(cvType.getXref());					
		T toReturn = findOrCreateControlledVocabulary(term, bpXref, bpCvClass);

		return toReturn;
 	}

	
	/*
	 * Given a term (name), unification xref, it finds/creates and returns a ControlledVocabulary.
	 */
	private <T extends ControlledVocabulary> T findOrCreateControlledVocabulary(
			String term, UnificationXref bpXref, Class<T> bpCvClass) {

		// generate URI
		String uri = xmlBase + bpCvClass.getSimpleName() + "_" + 
			encode(
				(term != null && !term.isEmpty()) ? term : bpXref.getDb() + "_" + bpXref.getId()
			);
		
		// look for name in our vocabulary set
		T toReturn = (T) bpModel.getByID(uri);
		if (toReturn != null) 
			return toReturn;

		// create/add a new controlled vocabulary	
		toReturn = bpModel.addNew(bpCvClass, uri);
		if(term!=null)
			toReturn.addTerm(term);
		
		toReturn.addXref(bpXref);

		return toReturn;
 	}

	
	private Set<Xref> getXrefs(psidev.psi.mi.xml.model.Xref psiXREF) {

		// set to return
		Set<Xref> toReturn = new HashSet<>();

		// check args
		if (psiXREF == null) return toReturn;
		
		// create the list of all other psimi xrefs
		List<DbReference> psiDBRefList = new ArrayList<>();

		DbReference primaryRef = psiXREF.getPrimaryRef();
		if(primaryRef != null) { //looks, it should never be null in a valid PSI-MI model
            if ("identity".equals(primaryRef.getRefType()) || "identical object".equals(primaryRef.getRefType())) {
                UnificationXref bpXref = unificationXref(primaryRef.getDb(), primaryRef.getId());
				toReturn.add(bpXref);
            } else {
				psiDBRefList.add(psiXREF.getPrimaryRef());
			}
		}

		if (psiXREF.hasSecondaryRef()) {
			psiDBRefList.addAll(psiXREF.getSecondaryRef());
		}

		for (DbReference psiref : psiDBRefList) {
			if(psiref==null) 
				continue;
			xref(psiref, toReturn);
        }

		return toReturn;
	}


	private void xref(DbReference psiref, Set<Xref> bpXrefs) {
		// process ref type
		String refType = (psiref.hasRefType()) ? psiref.getRefType() : null;
		String refTypeAc = (psiref.hasRefTypeAc()) ? psiref.getRefTypeAc() : null;
        String psiDBRefId = psiref.getId();
        String psiDBRefDb = psiref.getDb();
        // If multiple ids given with comma separated values, then split them.
        for (String dbRefId : psiDBRefId.split(",")) {	
        	Xref bpXref = null;
			// Let's not make UnificationXrefs. RelationshipXref is more safe.
			// Often, a gene or omim ID (can be another species') is a protein's xref id with 'identity' type...
            if(!"pubmed".equalsIgnoreCase(psiDBRefDb)) {
				bpXref = relationshipXref(psiDBRefDb, dbRefId, refType, refTypeAc);
            }
            else {
            	//TODO shall we skip PublicationXref here (IntAct puts the same PSIMI paper pmid everywhere...)?
				bpXref = publicationXref(psiDBRefDb, dbRefId);
            }

            if (bpXref != null) 
            	bpXrefs.add(bpXref);
        }
	}


	private String dbQuickFix(String db) {
        //a hack/fix to have the standard name of the gene id resource
        if("entrezgene/locuslink".equalsIgnoreCase(db)
        		|| "entrezgene".equalsIgnoreCase(db)
        		|| "entrez gene".equalsIgnoreCase(db)
        		|| "ncbi gene".equalsIgnoreCase(db)
        		|| "geneid".equalsIgnoreCase(db)
        		|| "gene id".equalsIgnoreCase(db)
        		|| "ncbigene".equalsIgnoreCase(db))
        	db = "NCBI Gene";
        
		return db;
	}


	private UnificationXref getPrimaryUnificationXref(psidev.psi.mi.xml.model.Xref psiXref) {
		
		if (psiXref==null || psiXref.getPrimaryRef() == null) 
			return null;
		
		UnificationXref toReturn = null;
		
		DbReference psiDBRef = psiXref.getPrimaryRef();
		String refType = (psiDBRef.hasRefType()) ? psiDBRef.getRefType() : null;
        
        // If multiple ids given with comma separated values, then split them.
       	if (refType==null || "identity".equals(refType) || "identical object".equals(refType)) {
       		toReturn = unificationXref(psiDBRef.getDb(), psiDBRef.getId());
       	} 

		return toReturn;
	}
	
	
	private RelationshipXref getInteractorPrimaryRef(psidev.psi.mi.xml.model.Xref psiXref) {
		
		if (psiXref==null || psiXref.getPrimaryRef() == null) 
			return null;
		
		DbReference psiDBRef = psiXref.getPrimaryRef();		
		String refType = (psiDBRef.hasRefType()) ? psiDBRef.getRefType() : null;
		String refTypeAc = (psiDBRef.hasRefType()) ? psiDBRef.getRefTypeAc() : null;
       	
		return relationshipXref(psiDBRef.getDb(), psiDBRef.getId(), refType, refTypeAc);
	}
	
	
	private static final Collection<String> BAD_ID_VALS = 
			Arrays.asList("0", "-1", "NULL", "NIL", "NONE", "N/A");
	
	private UnificationXref unificationXref(String db, String id) {
		if(db == null || db.trim().isEmpty()) {
			LOG.warn("unificationXref(), db is null, id=" + id);
			return null;
		}
		
		db = dbQuickFix(db);
		
		if(id == null || id.trim().isEmpty() || BAD_ID_VALS.contains(id.trim().toUpperCase())) {
			LOG.warn("unificationXref(), illegal or empty id:" + id);
			return  null;
		}
		
		id = id.trim();
		String xuri = xmlBase + "UnificationXref_" + encode(db.toLowerCase() + "_" + id);
		UnificationXref x = (UnificationXref) bpModel.getByID(xuri);
		if(x==null) {
			x= bpModel.addNew(UnificationXref.class, xuri);
			x.setDb(db);
			x.setId(id);
		}
		return x;
	}
	
	private PublicationXref publicationXref(String db, String id) {
		if(db == null || db.isEmpty()) {
			LOG.warn("publicationXref(), db is null, id=" + id);
			return null;
		}
		
		db = dbQuickFix(db);
		
		if(id == null || id.trim().isEmpty() || BAD_ID_VALS.contains(id.trim().toUpperCase())) {
			LOG.warn("publicationXref(), skip illegal id=" + id);
			return  null;
		}
		
		id = id.trim();

		//add only if it's a valid PMID, and not the default (IntAct) one
		if(("pubmed".equalsIgnoreCase(db) && !id.matches("\\d+")) || "14755292".equals(id)) {
			LOG.warn("publicationXref(), skip illegal or dummy publication id=" + id);
			return  null;
		}
		
		String xuri = xmlBase + "PublicationXref_" + encode(db.toLowerCase() + "_" + id);
		PublicationXref x = (PublicationXref) bpModel.getByID(xuri);
		if(x==null) {
			x= bpModel.addNew(PublicationXref.class, xuri);
			x.setDb(db);
			x.setId(id);
		}
		return x;
	}	


	private String encode(String id) {
			return id.replaceAll("[^-\\w]", "_");
	}


	/*
	 * Given a psi-mi experiment type, returns a set of open
	 * controlled vocabulary objects which represent evidence code(s).
	 */
	private Set<EvidenceCodeVocabulary> getEvidenceCodes(ExperimentDescription experimentDescription) {
		Set<EvidenceCodeVocabulary> toReturn = new HashSet<>();

		// get experiment methods
		Set<CvType> cvTypeSet = new HashSet<>(3);
		//skip if "unspecified method" (MI:0686) is the interaction detection method
		if(experimentDescription.getInteractionDetectionMethod() != null 
				&& !"MI:0686".equalsIgnoreCase(experimentDescription.getInteractionDetectionMethod().getXref().getPrimaryRef().getId()))
			cvTypeSet.add(experimentDescription.getInteractionDetectionMethod());
		if(experimentDescription.getParticipantIdentificationMethod() != null 
				&& !"MI:0686".equalsIgnoreCase(experimentDescription.getParticipantIdentificationMethod().getXref().getPrimaryRef().getId()))
			cvTypeSet.add(experimentDescription.getParticipantIdentificationMethod());
		if(experimentDescription.getFeatureDetectionMethod() != null 
				&& !"MI:0686".equalsIgnoreCase(experimentDescription.getFeatureDetectionMethod().getXref().getPrimaryRef().getId()))
			cvTypeSet.add(experimentDescription.getFeatureDetectionMethod());

		// create openControlledVocabulary objects for each detection method
		for (CvType cvtype : cvTypeSet) {
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
		Set<Xref> bpXrefs = new HashSet<>();
		if (ocv != null && ocv.getXref() != null) {
			bpXrefs.addAll(getXrefs(ocv.getXref()));
		}

		// used to store names and attributes
		Set<String> comments = new HashSet<>();

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
		Set<String> toReturn = new HashSet<>();

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

	/* Builds a new ExperimentalForm.
	 * 
	 * @param expEntity - if null, then either the interaction participant entity
	 * 					itsef is the experimental entity, or exp. interactor is unspecified;
	 * 					in both cases we set ExperimentalForm.experimentalFormEntity to null
	 * 					to avoid endless loop: entity/evidence/form/entity/evidence/...
	 * @param participant - PSI-MI interaction's participant object;
	 * @param exp - interaction's experiment desc. or null (which actually means all experiments)
	 */
	private void createAddExperimentalForm(Participant participant, 
			Evidence pEvidence, Entity expEntity, ExperimentDescription exp) 
	{	 		
		Set<EntityFeature> efs = new HashSet<>();
		for(Feature f : participant.getFeatures()) {
			if(exp==null || f.getExperiments().contains(exp) || f.getExperiments().isEmpty()) {
				EntityFeature feature = getFeature(ModificationFeature.class, f);			
				if(feature != null) {
					efs.add(feature);
				}
			}
		}

		Set<ExperimentalFormVocabulary> efvs = new HashSet<>();
		if (participant.hasExperimentalRoles()) {
			for (ExperimentalRole role : participant.getExperimentalRoles()) {
				if(exp==null || role.getExperiments().contains(exp) || role.getExperiments().isEmpty()) {
					//create/find ExperimentalFormVocabulary (skip for "unspecified role")
					ExperimentalFormVocabulary efv = null;
					//if(experimentalRole.hasNames() && !"unspecified role".equalsIgnoreCase(experimentalRole.getNames().getShortLabel()))
					if(role.getXref() != null && !"MI:0499".equalsIgnoreCase(role.getXref().getPrimaryRef().getId())) {
						efv = findOrCreateControlledVocabulary(role, ExperimentalFormVocabulary.class);
						if(efv != null) {
							efvs.add(efv);
						}
					}
				}
			}
		}
		
		if( efs.isEmpty() && efvs.isEmpty() && expEntity == null)
			return; //skip creating empty useless ExperimentalForm		
		
		ExperimentalForm ef = bpModel.addNew(ExperimentalForm.class, genUri(ExperimentalForm.class));
		
		//add all exp. features to the exp. form
		if(!efs.isEmpty()) {
			ef.addComment(efs.toString());
			for(EntityFeature f : efs)
				ef.addExperimentalFeature(f);
		}
		
		//add all exp. form vocabularies (roles) to the exp. form
		if(!efvs.isEmpty()) {
			ef.addComment(efvs.toString());
			for(ExperimentalFormVocabulary efv : efvs)
				ef.addExperimentalFormDescription(efv);
		}
				
		if(expEntity != null) {
			ef.setExperimentalFormEntity(expEntity);
			ef.addComment(EXPERIMENTAL_FORM_ENTITY_COMMENT + ": " + expEntity.getDisplayName());
		}

		//add the ef to participant's evidence
		pEvidence.addExperimentalForm(ef);

	}

	/*
	 * Given a PSI Names object, returns a name.
	 */
	private String getName(Names name) {		

		if (name.hasShortLabel()) {
			return name.getShortLabel();
		}
		else if (name.hasFullName()) {
			return name.getFullName();
		}

		return null;
	}


	private RelationshipXref relationshipXref(String db, String id, String refType, String refTypeAc)
	{	
		if(db == null || db.trim().isEmpty()) {
			LOG.warn("relationshipXref(), db is null, id=" + id);
			return null;
		}
		
		db = dbQuickFix(db);
		
		if(id == null || id.trim().isEmpty() || BAD_ID_VALS.contains(id.trim().toUpperCase())) {
			LOG.warn("relationshipXref(), illegal id=" + id);
			return  null;
		}
		
		id = id.trim();
		
		//generate URI
		String uri = xmlBase + "RelationshipXref_";
		if(refType!=null && !refType.isEmpty())
			uri += encode(db.toLowerCase()+"_"+id+"_"+refType);
		else
			uri += encode(db.toLowerCase()+"_"+id);	
		
        RelationshipXref x = (RelationshipXref) bpModel.getByID(uri);                 
        
        if (x == null) { //create/add a new RX
        	x = bpModel.addNew(RelationshipXref.class, uri);
        	x.setDb(db);
        	x.setId(id);
        	if (refType != null) //use the standard CV term and accession
        	{
        		String cvUri = (refTypeAc!=null) ? "http://identifiers.org/psimi/" + refTypeAc
        			: xmlBase + "RTV_" + encode(refType);//the latter should not happen often (ever, in a valid PSI-MI XML)			
        		RelationshipTypeVocabulary rtv = (RelationshipTypeVocabulary) bpModel.getByID(cvUri);
        		if(rtv == null) {
        			rtv = bpModel.addNew(RelationshipTypeVocabulary.class, cvUri);
        			rtv.addTerm(refType);
        			if(refTypeAc != null && !refTypeAc.isEmpty()) {//null happens, e.g., for 'uniprot-removed-ac' terms...
        				UnificationXref cvx = bpModel.addNew(UnificationXref.class, genUri(UnificationXref.class));
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
	 * Gets a confidence/score object.
	 */
	private Score createScore(String value, 
			Set<? extends Xref> bpXrefs, Set<String> comments)
	{
		Score bpScore = bpModel.addNew(Score.class, genUri(Score.class, value));
		if (value != null)
		{
			bpScore.setValue(value);
		}
		if (bpXrefs != null && !bpXrefs.isEmpty())
		{
			for (Xref xref : bpXrefs) {
				bpScore.addXref(xref);
			}
		}
		if (comments != null && !comments.isEmpty())
		{
			for (String comment : comments) {
				bpScore.addComment(comment);
			}
		}
		return bpScore;
	}


	/*
	 * New a molecular interaction.
	 */
	private MolecularInteraction createMolecularInteraction(
			Set<? extends Entity> participants,
			Set<InteractionVocabulary> interactionVocabularies,
			Object... psimiIds)
	{
		MolecularInteraction toReturn =
				bpModel.addNew(MolecularInteraction.class, genUri(MolecularInteraction.class, psimiIds));

		if (participants != null && !participants.isEmpty())
		{
			for (Entity participant : participants) {
				toReturn.addParticipant(participant);
			}
		}
			
		for(InteractionVocabulary iv : interactionVocabularies) {
			toReturn.addInteractionType(iv);
		}
			
		return toReturn;
	}
	
	private GeneticInteraction createGeneticInteraction(
			Set<? extends Entity> participants,
			Set<InteractionVocabulary> interactionVocabularies,
			Object... psimiIds)
	{
		GeneticInteraction toReturn =
				bpModel.addNew(GeneticInteraction.class, genUri(GeneticInteraction.class, psimiIds));

		if (participants != null && !participants.isEmpty())
		{
			for (Entity participant : participants) {
				toReturn.addParticipant((Gene)participant);
			}
		}

		for(InteractionVocabulary iv : interactionVocabularies) {
			toReturn.addInteractionType(iv);
		}

		return toReturn;
	}


	private Complex createComplex(Set<? extends Entity> participants, Object... psimiIds)
	{
		Complex toReturn = bpModel.addNew(Complex.class, genUri(Complex.class, psimiIds));

		if (participants != null && !participants.isEmpty())
		{
			for (Entity participant : participants) {
				toReturn.addComponent((PhysicalEntity)participant);
			}
		}
			
		return toReturn;
	}
	

	private <T extends EntityFeature> T getFeature(Class<T> featureClass, Feature psiFeature)
	{					
		String entityFeatureUri = genUri(featureClass, psiFeature.getId());
		T entityFeature = (T) bpModel.addNew(featureClass, entityFeatureUri);
		
		// feature location
		Set<SequenceInterval> featureLocations = getSequenceLocation(psiFeature.getRanges());
		if (featureLocations != null)
			for (SequenceLocation featureLocation : featureLocations) 
				entityFeature.setFeatureLocation(featureLocation);
		
		// set biopax featureLocationType prop. (find/create a SequenceRegionVocabulary)
		String term = null;
		if (psiFeature.hasNames())
			term = getName(psiFeature.getNames());
		//xref and primaryRef must exist
		UnificationXref uref = getPrimaryUnificationXref(psiFeature.getXref());
		SequenceRegionVocabulary srv = findOrCreateControlledVocabulary(term, 
				uref, SequenceRegionVocabulary.class); //can be null
		entityFeature.setFeatureLocationType(srv);				
		
		if (psiFeature.hasFeatureType()) {
			// set modificationType (get/create a SequenceModificationVocabulary)
			if(featureClass == ModificationFeature.class) {
				SequenceModificationVocabulary smv = findOrCreateControlledVocabulary(
						psiFeature.getFeatureType(), SequenceModificationVocabulary.class); //can be null
				((ModificationFeature)entityFeature).setModificationType(smv);
			} 
		}
		
		return entityFeature;
	}

	
	private SequenceInterval getSequenceLocation(long beginSequenceInterval,
	                                            long endSequenceInterval)
	{
			SequenceInterval toReturn = bpModel.addNew(SequenceInterval.class,
							genUri(SequenceInterval.class, beginSequenceInterval,endSequenceInterval));
			SequenceSite bpSequenceSiteBegin = bpModel.addNew(SequenceSite.class,
					genUri(SequenceSite.class, beginSequenceInterval));
			bpSequenceSiteBegin.setSequencePosition((int) beginSequenceInterval);
			toReturn.setSequenceIntervalBegin(bpSequenceSiteBegin);
			SequenceSite bpSequenceSiteEnd = bpModel.addNew(SequenceSite.class,
					genUri(SequenceSite.class, endSequenceInterval));
			bpSequenceSiteEnd.setSequencePosition((int) endSequenceInterval);
			toReturn.setSequenceIntervalEnd(bpSequenceSiteEnd);
			
			return toReturn;
	}
	

	/*
	 * Given a set of evidence objects, determines if interaction (that evidence obj is derived from)
	 * is a genetic interaction.
	 */
	private boolean isGeneticInteraction(Set<Evidence> bpEvidence)
	{
			if (bpEvidence != null && !bpEvidence.isEmpty()) {
				for (Evidence e : (Set<Evidence>) bpEvidence) {
					Set<EvidenceCodeVocabulary> evidenceCodes = e.getEvidenceCode();
					if (evidenceCodes != null) {
						for (EvidenceCodeVocabulary cv : evidenceCodes) {
							Set<String> terms = cv.getTerm();
							if (terms != null) {
								for (String term : terms) {
									if (GENETIC_INTERACTIONS.contains(term.toLowerCase()))
										return true;
								}
							}
						}
					}
				}
			}
		return false;
	}

	/*
	 * Generates a URI of a BioPAX object using the xml base,
	 * original xml ids (optional, for debugging),
	 * BioPAX model interface name and some unique number.
	 * The idea is virtually never ever return the same URI here (taking into account 
	 * that there are multiple threads converting different PSI-MI Entries, one per thread,
	 * simultaneously)
	 */
	private String genUri(Class<? extends BioPAXElement> type, Object... psimiIds) {
		return xmlBase + type.getSimpleName() + "_" + UUID.randomUUID() +
				( psimiIds.length>0 ? "_" + encode(ArrayUtils.toString(psimiIds)) : "");
	}
}
