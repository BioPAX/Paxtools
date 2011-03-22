package org.biopax.paxtools.model.level3;

import org.biopax.paxtools.model.BioPAXFactory;

/**
 */
public interface Level3Factory extends BioPAXFactory {
    public BiochemicalReaction createBiochemicalReaction();

    public BioSource createBioSource();

    public Catalysis createCatalysis();

    public ChemicalStructure createChemicalStructure();

    public Complex createComplex();

    public ComplexAssembly createComplexAssembly();

    public Degradation createDegradation();

    public Stoichiometry createStoichiometry();

    public Score createScore();

    public Control createControl();

    public Conversion createConversion();

    public ModificationFeature createModificationFeature();

    public BindingFeature createBindingFeature();

    public Provenance createProvenance();

    public DeltaG createDeltaG();

    public BiochemicalPathwayStep createBiochemicalPathwayStep();

    public Dna createDna();

    public EntityFeature createEntityFeature();

    public Evidence createEvidence();

    public ExperimentalForm createExperimentalForm();

    public Gene createGene();

    public Interaction createInteraction();

    public KPrime createKPrime();

    public Modulation createModulation();

    public ControlledVocabulary createControlledVocabulary();

    public Pathway createPathway();

    public PathwayStep createPathwayStep();

    public PhysicalEntity createPhysicalEntity();

    public MolecularInteraction createMolecularInteraction();

    public Protein createProtein();

    public PublicationXref createPublicationXref();

    public DnaReference createDnaReference();

    public RnaReference createRnaReference();

    public SmallMoleculeReference createSmallMoleculeReference();

    public ProteinReference createProteinReference();

    public RelationshipXref createRelationshipXref();

    public Rna createRna();

    public SequenceInterval createSequenceInterval();

    public SequenceLocation createSequenceLocation();

    public SequenceSite createSequenceSite();

    public SmallMolecule createSmallMolecule();

    public Transport createTransport();

    public TransportWithBiochemicalReaction createTransportWithBiochemicalReaction();

    public UnificationXref createUnificationXref();

    public TissueVocabulary createTissueVocabulary();

    public CellVocabulary createCellVocabulary();

    public CellularLocationVocabulary createCellularLocationVocabulary();

    public ExperimentalFormVocabulary createExperimentalFormVocabulary();

    public EvidenceCodeVocabulary createEvidenceCodeVocabulary();

    public SequenceRegionVocabulary createSequenceRegionVocabulary();

    public SequenceModificationVocabulary createSequenceModificationVocabulary();

    public RelationshipTypeVocabulary createRelationshipTypeVocabulary();

    public FragmentFeature createFragmentFeature();

    public TemplateReaction createTemplateReaction();

    public TemplateReactionRegulation createTemplateReactionRegulation();

    public DnaRegion createDnaRegion();

    public DnaRegionReference createDnaRegionReference();

    public RnaRegion createRnaRegion();

    public RnaRegionReference createRnaRegionReference();

    public InteractionVocabulary createInteractionVocabulary();

    public PhenotypeVocabulary createPhenotypeVocabulary();

    public CovalentBindingFeature createCovalentBindingFeature();

    public GeneticInteraction createGeneticInteraction();
}
