package org.biopax.paxtools.converter.psi;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

import psidev.psi.mi.xml.model.*;


/**
 * PSIMI 'entry' to BioPAX converter.
 *
 * @author Benjamin Gross, rodche (major re-factoring for Level3; fixing, adding genetic interactions)
 */
class EntryMapper {

	private static final Log LOG = LogFactory.getLog(EntryMapper.class);
	
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
	
	private final Model bpModel;
	
	private final String xmlBase;
	
	private long counter;
	
	private final boolean forceInteractionToComplex;

	// a map from psi-mi participant to biopax physical entity or gene
	private final Map<Participant, Entity> participantMap;
	
	/**
	 * Constructor.
	 *
	 * @param model
	 * @param forceInteractionToComplex - always generate Complex instead of MolecularInteraction
	 */
	public EntryMapper(Model model, boolean forceInteractionToComplex) {
		this.bpModel = model;
		this.xmlBase = model.getXmlBase();
		this.counter = System.currentTimeMillis();
		this.forceInteractionToComplex = forceInteractionToComplex;
		this.participantMap = new HashMap<Participant, Entity>();
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
		final Set<String> avail = new HashSet<String>();
		if(entry.hasAvailabilities()) {
			for (Availability a : entry.getAvailabilities())
				if (a.hasValue()) 
					avail.add(a.getValue());	
		}
		
		// get data source
		final Provenance pro = createProvenance(entry.getSource());
		
		//a skip-set of interactions linked by participant.interactionRef element (complex blocks)
		Set<Interaction> participantInteractions = new HashSet<Interaction>();
		for(Interaction interaction : entry.getInteractions()) {
			for(Participant participant : interaction.getParticipants()) {
				if(participant.hasInteraction()) {
					//ignore hasInteractionRef/getInteractionRefs, for these get cleared by the psimi parser
					participantInteractions.add(participant.getInteraction());
				}
			}
		}
		
		// iterate through the root interactions and create biopax interactions or complexes
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
				: genUri(Provenance.class, bpModel);
		
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
	 * psi.interactionElementType                 -> biopax Complex, MolecularInteraction, or GeneticInteraction
	 * psi.interactionElementType.participantList -> biopax interaction/complex participants/components
	 */
	private Entity processInteraction(Interaction interaction, Set<String> avail, Provenance pro, boolean isComplex) {
		
		Entity bpEntity = null; //interaction or complex
		
		// get interaction name/short name
		String name = null;
		String shortName = null;
		if (interaction.hasNames()) {
			Names names = interaction.getNames();
			name = (names.hasFullName()) ? names.getFullName() : "";
			shortName = (names.hasShortLabel()) ? names.getShortLabel() : "";
		}
		
		// interate through the psi participants, create biopax equivalents
		Set<Entity> bpParticipants = new HashSet<Entity>();
		for (Participant participant : interaction.getParticipants()) {
			// get paxtools physical entity participant and add to participant list
			Entity bpParticipant = createParticipant(participant, interaction, avail, pro);
			if (bpParticipant != null) {
				if(!bpParticipants.contains(bpParticipant))
					bpParticipants.add(bpParticipant);
				participantMap.put(participant, bpParticipant);
			}
		}
				
		Set<InteractionVocabulary> interactionVocabularies = new HashSet<InteractionVocabulary>();
		if (interaction.hasInteractionTypes()) {
			for(CvType interactionType : interaction.getInteractionTypes()) {
				//generate InteractionVocabulary and set interactionType
				InteractionVocabulary cv = findOrCreateControlledVocabulary(interactionType, InteractionVocabulary.class);
				if(cv != null)
					interactionVocabularies.add(cv);
			}
		}
		
		// as of BioGRID v3.1.72 (at least), genetic interaction code can reside
		// as an attribute of the Interaction via "BioGRID Evidence Code" key
		boolean isGeneticInteraction = false;
		if (interaction.hasAttributes()) {
			for (Attribute attribute : interaction.getAttributes()) {
				if (attribute.getName().equalsIgnoreCase(BIOGRID_EVIDENCE_CODE)) {
					String value = (attribute.hasValue()) ? attribute.getValue().toLowerCase() : "";
					if (GENETIC_INTERACTIONS.contains(value)) {
						isGeneticInteraction = true;
					}
				}
			}
		}
		
		// get experiment metadata; also helps us determine if interaction is genetic or not
		Set<Evidence> bpEvidences = new HashSet<Evidence>();
		if (interaction.hasExperiments()) {			
			for (ExperimentDescription experimentDescription : interaction.getExperiments()) {
				// build and add evidence
				bpEvidences.add(createEvidence(interaction, experimentDescription));
			}		
		}	
		
		//check another genetic interaction flag (criteria)
		if(!isGeneticInteraction) 
			isGeneticInteraction = isGeneticInteraction(bpEvidences);
		
		//last test and hack (to skip for e.g. IntAct gene-protein (TF) interactions, 
		//TODO looks, we should convert such to a TemplateReaction...
		Set<Class<? extends BioPAXElement>> participantTypes = new HashSet<Class<? extends BioPAXElement>>();
		for(Entity p : bpParticipants)
			participantTypes.add(p.getModelInterface());
		if(participantTypes.size() > 1 && participantTypes.contains(Gene.class)) {
			isGeneticInteraction = false;
			LOG.warn("Skipped a gene-notgene interaction; psimi-id=" 
					+ interaction.getId() + ", name(s): " + shortName + " " + name 
					+ "; participants: " + participantTypes);
			return null;
		}
		
		if ((isComplex || forceInteractionToComplex) && !isGeneticInteraction) {
			bpEntity = createComplex(bpParticipants, bpEvidences);
		} else if(isGeneticInteraction) {
			bpEntity = createGeneticInteraction(bpParticipants, bpEvidences, interactionVocabularies);
		} else {
			bpEntity = createMolecularInteraction(bpParticipants, bpEvidences, interactionVocabularies);
		}
		
		addAvailabilityAndProvenance(bpEntity, avail, pro);
		
		if (name != null)
			bpEntity.setStandardName(name);
		if (shortName != null)
			bpEntity.setDisplayName(shortName);
				
		// add xrefs		
		Set<Xref> bpXrefs = new HashSet<Xref>();		
		if (interaction.hasXref()) {
			bpXrefs.addAll(getXrefs(interaction.getXref()));
		}
		
		for (Xref bpXref : bpXrefs) {
			bpEntity.addXref(bpXref);
		}
		
		return bpEntity;
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
	 * Converts PSIMI participant to
	 * BioPAX physical entity (and entity reference,
	 * and experimental form with exp. entity features)
	 * or gene.
	 *
	 * Note:
	 * psi.participantType -> PhysicalEntity or Gene
	 */
	private Entity createParticipant(Participant participant, Interaction interaction, Set<String> avail, Provenance pro) {
		
		//PSIMI parser does not set 'interactorRef' (or clears it after all), but does set (or infer) 'interactor' (see junit tests).
		Interactor interactor = null;
		if (participant.hasInteractor()) {
			interactor = participant.getInteractor();
		} else if(participant.hasInteraction()) {
			//hierarchical buildup of a complex (participant.hasInteraction==true)...
			Complex c = (Complex) processInteraction(participant.getInteraction(), avail, pro, true);
			return c; //done
		}

		if (interactor == null) {
			LOG.error("EntryMapper.createParticipant(): interactor cannot be found;"
				+ " participant: " + participant.toString());
			return null;
		}

		// Find or create the physical entity and entity reference -
		
		// figure out physical entity type (protein, dna, rna, small molecule)
		String physicalEntityType = null;
		CvType interactorType = interactor.getInteractorType();
		if (interactorType != null && interactorType.hasNames()) {
			physicalEntityType = getName(interactorType.getNames());
		}

		// get names/synonyms from the psimi interactor (participant does not have them)
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
		
		Set<Xref> bpXrefsOfInteractor = getXrefs(interactor.getXref());
		
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
		if ("small molecule".equalsIgnoreCase(physicalEntityType))
		{
			entityClass = SmallMolecule.class;
			entityReferenceClass = SmallMoleculeReference.class;
		} else if ("dna".equalsIgnoreCase(physicalEntityType))
		{
			entityClass = Dna.class;
			entityReferenceClass = DnaReference.class;
		} else if ("rna".equalsIgnoreCase(physicalEntityType))
		{
			entityClass = Rna.class;
			entityReferenceClass = RnaReference.class;
		} else if ("gene".equalsIgnoreCase(physicalEntityType))
		{
			entityClass = Gene.class;
			entityReferenceClass = null;
		} //else it's still a protein (peptides are proteins too)
		
		//make consistent biopax URI (for the entity ref., if applies, or phys. entity)
		String entityUri = null; //always make new unique URI (avoid different states clash)
		String baseUri = xmlBase;
		if(entityReferenceClass != null)
			baseUri += entityReferenceClass.getSimpleName() + "_";		
		
		RelationshipXref x = getPrimaryRelationshipXref(interactor.getXref());
		if(x != null) {
			baseUri += encode(x.getDb() + "_" + x.getId());
			if(x.getRelationshipType()!=null)
				baseUri += "_" + encode(x.getRelationshipType().getTerm().iterator().next());
			entityUri = baseUri + "_" + entityClass.getSimpleName() + "_" + (counter++);
			x=null; //not needed
		} else { //when no xrefs present, use a number ending (always increment);
			baseUri += "_" + (counter++); //new unique part (seldom happens, when no primary xref...)
			entityUri = baseUri + "_" + entityClass.getSimpleName();		
		}
		
		if(cellularLocation != null)
			entityUri += "_" + encode(cellularLocation.getTerm().iterator().next());
		
		// always create a new PE or Gene 
		//(not's add to the model just yet; there could be an existing equivalent one)
		Entity entity = bpModel.getLevel().getDefaultFactory().create(entityClass, entityUri);
		addAvailabilityAndProvenance(entity, avail, pro);
		
		//and set names
		if (name != null) {
			//quite a few PSIMI providers use too long text (like comments) for <fullName> fields...
			if(name.length() > 50 && shortName != null) 
				entity.addComment(name);
			else 
				entity.setStandardName(name);
		}
		if (shortName != null) {
            entity.setDisplayName(shortName);
		}		
		
		// set cell. loc.
		if(cellularLocation != null && entity instanceof PhysicalEntity)
			((PhysicalEntity)entity).setCellularLocation(cellularLocation);
		
		// when not a Gene, we are to find/generate corresponding EntityRererence;
		// but we do not want to gererate apparently duplicate objects...
		if(entityReferenceClass != null) {
			EntityReference entityReference = null;
			BioSource bioSource = getBioSource(interactor);
			
			/* check if the entity ref. with this URI already exists 
			 * (we'll the use that instead of generating a new one every time;
			 * unfortunately, chances still are that we generate same URI 
			 * from non-equivalent psi-mi interactors, though this would probably 
			 * flag for semantic errors in the psi-mi data, such as using same xrefs,
			 * features, etc. for different xml entries unnecessarily).
			 * Unfortunately, we cannot simply create a new ER and simply compare
			 * with an existing one using Paxtools (as of paxtools-core 4.x and older, 
			 * two ERs are NOT equivalent unless they either have the same URI, or - same organism and sequence).
			 */
			EntityReference er = (EntityReference) bpModel.getByID(baseUri);			
			if( er != null 
				&& er.getModelInterface()==entityReferenceClass 
				&& er.getXref().size() == bpXrefsOfInteractor.size() //ideally we'd compare all xrefs there...
				&& ((er.getDisplayName()==null && shortName==null) 
						|| (er.getDisplayName()!=null && er.getDisplayName().equalsIgnoreCase(shortName))
					)
				&& (bioSource != null && SequenceEntityReference.class.isAssignableFrom(er.getModelInterface())
					&& (bioSource.getDisplayName()!=null && bioSource.getDisplayName().equalsIgnoreCase(((SequenceEntityReference)er).getOrganism().getDisplayName()))
					)
			) 
			{
				entityReference = er; // ok to reuse
			} else if( er != null ) {
				LOG.warn("Model has a different (non-equivalent) " + er.getModelInterface().getSimpleName()
							+ " with uri=" + baseUri + "; so, I will make a new ER using another URI.");
				baseUri += "_" + (counter++);
			}
			
			//if not found above, create a new ER
			if(entityReference == null) { 
				//generate a new ER
				entityReference = bpModel.addNew(entityReferenceClass, baseUri);	
				
				//set ER's names, xrefs
				if (name != null) {
					if(name.length() > 50 && //and there are other names available
							(shortName!=null || (synonyms!=null && !synonyms.isEmpty())))
						entityReference.addComment(name); //comment instead of 'standardName'
					else		
						entityReference.setStandardName(name);
				}
				if (shortName != null) {
					entityReference.setDisplayName(shortName);
				}
				if (synonyms != null) {
					for (String synonym : synonyms)
						entityReference.addName(synonym);
				}
				if (bpXrefsOfInteractor != null) {
					for (Xref xref : bpXrefsOfInteractor)
						entityReference.addXref((Xref) xref);
				}
				
				//set organism if it's not a small molecule
				if(entityReference instanceof SequenceEntityReference) {
					SequenceEntityReference ser = (SequenceEntityReference)entityReference;
					ser.setOrganism(bioSource);
					ser.setSequence(interactor.getSequence());
				}
			}
			
			
			
			// set ER
			((SimplePhysicalEntity)entity).setEntityReference(entityReference);		
			
		} else { //i.e., entity is Gene	(otherwise, entityReferenceClass != null by design of this psimi converter)
			assert entity instanceof Gene : "Must be Gene instead: " + entity.getModelInterface().getSimpleName();
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
			((Gene)entity).setOrganism(getBioSource(interactor));
		}
		
		//try to avoid duplicate entities
		boolean hasEquivalentEntity = false;
		for(BioPAXElement ety : bpModel.getObjects(entity.getModelInterface())) {
			if(ety.isEquivalent(entity)) {
				entity = (Entity) ety;
				hasEquivalentEntity = true;
				break;
			}	
		}
		
		if(!hasEquivalentEntity)
			bpModel.add(entity);
		
		return entity;
	}


	/*
	 * Given a psi feature list,
	 * adds biopax entity features 
	 * to the experimental form.
	 */
	private void addFeatures(ExperimentalForm ef, Collection<Feature> psiFeatureList) {

		if (psiFeatureList == null || psiFeatureList.isEmpty()) 
			return;

		for (Feature psiFeature : psiFeatureList) {
			if(psiFeature==null) continue;
			
			//TODO consider BindingFeature in some cases?..
			Class<? extends EntityFeature> featureClass = ModificationFeature.class; 		
			
			EntityFeature feature = getFeature(featureClass, psiFeature);			
			if(feature != null) 
				ef.addExperimentalFeature(feature);
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
	private BioSource getBioSource(Interactor interactor) {
		
		Organism organism = interactor.getOrganism();
		
		// trivial check
		if (organism == null) 
			return null;

		// cell type (can be undefined, i.e., null)
		CellVocabulary cellType = findOrCreateControlledVocabulary(organism.getCellType(), CellVocabulary.class);
		// tissue (can be null)
		TissueVocabulary tissue = findOrCreateControlledVocabulary(organism.getTissue(), TissueVocabulary.class);

		// set the BioPXElement URI and taxonomy xref id
		String ncbiId = Integer.toString(organism.getNcbiTaxId());
		String name = null;
		
		if (organism.hasNames()) {
			name = getName(organism.getNames());
			//a hack for BIND data (and perhaps other DBs)
			if((name.equalsIgnoreCase("homo sapiens")
					|| name.equalsIgnoreCase("human"))
					&& !ncbiId.equals("9606")) {
				LOG.warn("Based on the name, auto-changed taxonomy ID from " 
					+ ncbiId + " to 9606 (human) in: " + organism 
					+ "; interactor: " + interactor.getId());
				ncbiId = "9606";
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
		
		String taxonXrefUri = xmlBase + "UX_taxonomy_" + ncbiId;
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
			if(psiDBRef==null) 
				continue;
			
			// process ref type
			String refType = (psiDBRef.hasRefType()) ? psiDBRef.getRefType() : null;
			String refTypeAc = (psiDBRef.hasRefTypeAc()) ? psiDBRef.getRefTypeAc() : null;
            String psiDBRefId = psiDBRef.getId();
            String psiDBRefDb = psiDBRef.getDb();

            // If multiple ids given with comma separated values, then split them.
            for (String dbRefId : psiDBRefId.split(",")) {
            	Xref bpXref = null;
                if ("identity".equals(refType) || "identical object".equals(refType)) {
                    bpXref = unificationXref(psiDBRefDb, dbRefId);
                } 
                else if("secondary-ac".equals(refType) ) {
                	bpXref = unificationXref(psiDBRefDb, dbRefId);
                } 
                else if(!"pubmed".equalsIgnoreCase(psiDBRefDb)) {
                	bpXref = relationshipXref(psiDBRefDb, dbRefId, refType, refTypeAc);
                }
                else {
                	//TODO shall we skip PublicationXref here (IntAct puts the same PSIMI paper pmid everywhere...)?
            		bpXref = publicationXref(psiDBRefDb, dbRefId);
                }

                if (bpXref != null) 
                    toReturn.add(bpXref);
            }
        }

		return toReturn;
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
	
	
	private RelationshipXref getPrimaryRelationshipXref(psidev.psi.mi.xml.model.Xref psiXref) {
		
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
		if(db == null || db.isEmpty()) {
			LOG.warn("unificationXref(), db is null, id=" + id);
			return null;
		}
		if(id == null || BAD_ID_VALS.contains(id.toUpperCase())) {
			LOG.warn("unificationXref(), illegal id=" + id);
			return  null;
		}
		
		String xuri = xmlBase + "UX_" + encode(db.toLowerCase() + "_" + id);
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
		if(id == null || BAD_ID_VALS.contains(id.toUpperCase())) {
			LOG.warn("publicationXref(), illegal id=" + id);
			return  null;
		}
		
		String xuri = xmlBase + "PX_" + encode(db.toLowerCase() + "_" + id);
		PublicationXref x = (PublicationXref) bpModel.getByID(xuri);
		if(x==null) {
			x= bpModel.addNew(PublicationXref.class, xuri);
			x.setDb(db);
			x.setId(id);
		}
		return x;
	}	


	private String encode(String id) {
		try {
			return URLEncoder.encode(id, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return URLEncoder.encode(id);
		}
	}


	/*
	 * Given a psi-mi experiment type, returns a set of open
	 * controlled vocabulary objects which represent evidence code(s).
	 */
	private Set<EvidenceCodeVocabulary> getEvidenceCodes(ExperimentDescription experimentDescription) {
		Set<EvidenceCodeVocabulary> toReturn = new HashSet<EvidenceCodeVocabulary>();

		// get experiment methods
		Set<CvType> cvTypeSet = new HashSet<CvType>(3);
		cvTypeSet.add(experimentDescription.getInteractionDetectionMethod());
		cvTypeSet.add(experimentDescription.getParticipantIdentificationMethod());
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
		Set<Xref> bpXrefs = new HashSet<Xref>();
		if (ocv != null && ocv.getXref() != null) {
			bpXrefs.addAll(getXrefs(ocv.getXref()));
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
	 * Given psi-mi interaction and experiment, returns the set of experimental forms.
	 */
	private Set<ExperimentalForm> getExperimentalForms(Interaction interaction, ExperimentDescription experimentDescription) 
	{
		Set<ExperimentalForm> experimentalForms = new HashSet<ExperimentalForm>();		

		// interate through the psi participants, get experimental role
		for (Participant participant : interaction.getParticipants()) {
			// get participant - may be used in following loop
			Entity bpParticipant = participantMap.get(participant);			
			assert bpParticipant != null : "participantMap has now entry for a psimi participant key";
						
			// get experimental role list
			if (participant.hasExperimentalRoles()) {
				for (ExperimentalRole experimentalRole : participant.getExperimentalRoles()) {
					// according to psi-mi, no experimentalRefs means the participant plays in all experiments of the interaction;
					// (btw, no <experimentList> is defined under <experimentalRole> according to the xml schema)
					
					if (experimentalRole.hasExperiments() && !experimentalRole.getExperiments().contains(experimentDescription))
						continue; //skip: current experimentDescription is not listed in the not empty <experimentRefs> set
					
					//create or find and add the ExperimentalFormVocabulary
					ExperimentalFormVocabulary efv = 
						findOrCreateControlledVocabulary(experimentalRole, ExperimentalFormVocabulary.class);
					//create a EF
					String efUri = genUri(ExperimentalForm.class, bpModel) + 
							"_e" + experimentDescription.getId() + 
							"_i" + interaction.getId();
					ExperimentalForm experimentalForm =
							bpModel.addNew(ExperimentalForm.class, efUri);	
					experimentalForm.addExperimentalFormDescription(efv);						
					//only Gene or PE is in fact allowed to be set
					experimentalForm.setExperimentalFormEntity(bpParticipant);					
					experimentalForms.add(experimentalForm);
						
					//using participant.getFeatures(), set the ExperimentalForm/experimentalFeature values									
					addFeatures(experimentalForm, participant.getFeatures());						
				}
			}
		}

		return experimentalForms;
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
		if(db == null || db.isEmpty()) {
			LOG.warn("relationshipXref(), db is null, id=" + id);
			return null;
		}
		if(id == null || BAD_ID_VALS.contains(id.toUpperCase())) {
			LOG.warn("relationshipXref(), illegal id=" + id);
			return  null;
		}
		
		//generate URI
		String uri = xmlBase + "RX_";
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
	 * Builds an Evidence object.
	 */
	private Evidence createEvidence(Interaction interaction, ExperimentDescription experimentDescription)
	{
		String evUri = genUri(Evidence.class, bpModel) + 
				"_i" + interaction.getId() + "_e" + experimentDescription.getId();
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
		
		// experimental form
		Set<ExperimentalForm> experimentalForms = getExperimentalForms(interaction, experimentDescription);
		//TODO for entity features of these experimental forms, set BindingFeature.bindsTo (when <interaction><inferredInteractionList> elements are present, e.g., see IntAct)

		if (experimentalForms != null && !experimentalForms.isEmpty()) {
			for (ExperimentalForm experimentalForm : experimentalForms) {
				evidence.addExperimentalForm(experimentalForm);
			}
		}

		// interaction detection method, participant detection method, feature detection method
		Set<EvidenceCodeVocabulary> evidenceCodes = getEvidenceCodes(experimentDescription);			
		if(evidenceCodes != null)
			for(EvidenceCodeVocabulary ecv : evidenceCodes)
				evidence.addEvidenceCode(ecv);
			
		return evidence;
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
			Set<Evidence> bpEvidence,
			Set<InteractionVocabulary> interactionVocabularies)
	{
		MolecularInteraction toReturn =
				bpModel.addNew(MolecularInteraction.class, genUri(MolecularInteraction.class, bpModel));

		if (participants != null && !participants.isEmpty())
		{
			for (Entity participant : participants) {
				toReturn.addParticipant((PhysicalEntity)participant);
			}
		}
		if (bpEvidence != null && !bpEvidence.isEmpty())
		{
			for (Evidence evidence : bpEvidence) {
				toReturn.addEvidence(evidence);
			}
		}
			
		for(InteractionVocabulary iv : interactionVocabularies) {
			toReturn.addInteractionType(iv);
		}
			
		return toReturn;
	}
	
	private GeneticInteraction createGeneticInteraction(
			Set<? extends Entity> participants,
			Set<Evidence> bpEvidence,
			Set<InteractionVocabulary> interactionVocabularies)
	{
		GeneticInteraction toReturn =
				bpModel.addNew(GeneticInteraction.class, genUri(GeneticInteraction.class, bpModel));

		if (participants != null && !participants.isEmpty())
		{
			for (Entity participant : participants) {
				toReturn.addParticipant((Gene)participant);
			}
		}
		if (bpEvidence != null && !bpEvidence.isEmpty())
		{
			for (Evidence evidence : bpEvidence) {
				toReturn.addEvidence(evidence);
			}
		}

		for(InteractionVocabulary iv : interactionVocabularies) {
			toReturn.addInteractionType(iv);
		}

		return toReturn;
	}


	private Complex createComplex(
			Set<? extends Entity> participants,
			Set<Evidence> bpEvidence)
	{
		Complex toReturn = bpModel.addNew(Complex.class, genUri(Complex.class, bpModel));

		if (participants != null && !participants.isEmpty())
		{
			for (Entity participant : participants) {
				toReturn.addComponent((PhysicalEntity)participant);
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
	

	private <T extends EntityFeature> T getFeature(Class<T> featureClass, Feature psiFeature)
	{					
		String entityFeatureUri = genUri(featureClass, bpModel); 		
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
	 * Generates a URI of a BioPAX object using the xml base, model interface name 
	 * and generated number (sequential).
	 * The idea is virtually never ever return the same URI here (taking into account 
	 * that there are multiple threads converting different PSIMI Entries, one per thread, 
	 * simultaneously)
	 */
	private String genUri(Class<? extends BioPAXElement> type, Model model) {
		return xmlBase + type.getSimpleName() + "_" + (counter++);
	}	
}
