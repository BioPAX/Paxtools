package org.biopax.paxtools.model.level3;

import org.biopax.paxtools.model.BioPAXFactory;

public interface Level3Factory extends BioPAXFactory {
	
    BiochemicalReaction createBiochemicalReaction();

    BioSource createBioSource();

    Catalysis createCatalysis();

    ChemicalStructure createChemicalStructure();

    Complex createComplex();

    ComplexAssembly createComplexAssembly();

    Degradation createDegradation();

    Stoichiometry createStoichiometry();

    Score createScore();

    Control createControl();

    Conversion createConversion();

    ModificationFeature createModificationFeature();

    BindingFeature createBindingFeature();

    Provenance createProvenance();

    DeltaG createDeltaG();

    BiochemicalPathwayStep createBiochemicalPathwayStep();

    Dna createDna();

    EntityFeature createEntityFeature();

    Evidence createEvidence();

    ExperimentalForm createExperimentalForm();

    Gene createGene();

    Interaction createInteraction();

    KPrime createKPrime();

    Modulation createModulation();

    ControlledVocabulary createControlledVocabulary();

    Pathway createPathway();

    PathwayStep createPathwayStep();

    PhysicalEntity createPhysicalEntity();

    MolecularInteraction createMolecularInteraction();

    Protein createProtein();

    PublicationXref createPublicationXref();

    DnaReference createDnaReference();

    RnaReference createRnaReference();

    SmallMoleculeReference createSmallMoleculeReference();

    ProteinReference createProteinReference();

    RelationshipXref createRelationshipXref();

    Rna createRna();

    SequenceInterval createSequenceInterval();

    SequenceLocation createSequenceLocation();

    SequenceSite createSequenceSite();

    SmallMolecule createSmallMolecule();

    Transport createTransport();

    TransportWithBiochemicalReaction createTransportWithBiochemicalReaction();

    UnificationXref createUnificationXref();

    TissueVocabulary createTissueVocabulary();

    CellVocabulary createCellVocabulary();

    CellularLocationVocabulary createCellularLocationVocabulary();

    ExperimentalFormVocabulary createExperimentalFormVocabulary();

    EvidenceCodeVocabulary createEvidenceCodeVocabulary();

    SequenceRegionVocabulary createSequenceRegionVocabulary();

    SequenceModificationVocabulary createSequenceModificationVocabulary();

    RelationshipTypeVocabulary createRelationshipTypeVocabulary();

    FragmentFeature createFragmentFeature();

    TemplateReaction createTemplateReaction();

    TemplateReactionRegulation createTemplateReactionRegulation();

    DnaRegion createDnaRegion();

    DnaRegionReference createDnaRegionReference();

    RnaRegion createRnaRegion();

    RnaRegionReference createRnaRegionReference();

    InteractionVocabulary createInteractionVocabulary();

    PhenotypeVocabulary createPhenotypeVocabulary();

    CovalentBindingFeature createCovalentBindingFeature();

    GeneticInteraction createGeneticInteraction();
}
