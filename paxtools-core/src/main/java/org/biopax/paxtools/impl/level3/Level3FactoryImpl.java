package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXFactoryImpl;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.level3.*;

/**
 * User: root Date: Apr 26, 2006 Time: 3:06:08 PM_DOT
 */
public class Level3FactoryImpl extends BioPAXFactoryImpl implements Level3Factory
{
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXFactory ---------------------



	public BioPAXLevel getLevel()
	{
		return BioPAXLevel.L3;
	}

	public BioSource createBioSource()
	{
		return new BioSourceImpl();
	}

	public BiochemicalReaction createBiochemicalReaction()
	{
		return new BiochemicalReactionImpl();
	}

	public Catalysis createCatalysis()
	{
		return new CatalysisImpl();
	}

	public ChemicalStructure createChemicalStructure()
	{
		return new ChemicalStructureImpl();
	}

	public Complex createComplex()
	{
		return new ComplexImpl();
	}

	public ComplexAssembly createComplexAssembly()
	{
		return new ComplexAssemblyImpl();
	}

	public Interaction createInteraction()
	{
		return new InteractionImpl();
	}

	public MolecularInteraction createMolecularInteraction()
	{
		return new MolecularInteractionImpl();
	}

	public Score createScore()
	{
		return new ScoreImpl();
	}

	public Control createControl()
	{
		return new ControlImpl();
	}

	public Conversion createConversion()
	{
		return new ConversionImpl();
	}

	public org.biopax.paxtools.model.level3.DeltaG createDeltaG()
	{
		return new DeltaGImpl();
	}

	public BiochemicalPathwayStep createBiochemicalPathwayStep()
	{
		return new BiochemicalPathwayStepImpl();
	}

	public Dna createDna()
	{
		return new DnaImpl();
	}

	public EntityFeature createEntityFeature()
	{
		return new EntityFeatureImpl();
	}

	public Evidence createEvidence()
	{
		return new EvidenceImpl();
	}

	public ExperimentalForm createExperimentalForm()
	{
		return new ExperimentalFormImpl();
	}

    public Gene createGene()
    {
        return new GeneImpl();
    }

    public KPrime createKPrime()
	{
		return new KPrimeImpl();
	}

	public Modulation createModulation()
	{
		return new ModulationImpl();
	}

	public ControlledVocabulary createControlledVocabulary()
	{
		return new ControlledVocabularyImpl();
	}

	public Pathway createPathway()
	{
		return new PathwayImpl();
	}

	public PathwayStep createPathwayStep()
	{
		return new PathwayStepImpl();
	}

	public PhysicalEntity createPhysicalEntity()
	{
		return new PhysicalEntityImpl();     
	}


	public Protein createProtein()
	{
		return new ProteinImpl();
	}

	public PublicationXref createPublicationXref()
	{
		return new PublicationXrefImpl();
	}


	public DnaReference createDnaReference()
	{
		return new DnaReferenceImpl();
	}

	public EntityReference createEntityReference()
	{
		return new EntityReferenceImpl();
	}

    

    public RnaReference createRnaReference()
	{
		return new RnaReferenceImpl();
	}

	public SmallMoleculeReference createSmallMoleculeReference()
	{
		return new SmallMoleculeReferenceImpl();
	}

	public ProteinReference createProteinReference()
	{
		return new ProteinReferenceImpl();
	}

	public RelationshipXref createRelationshipXref()
	{
		return new RelationshipXrefImpl();
	}

	public Rna createRna()
	{
		return new RnaImpl();
	}

	public ModificationFeature createModificationFeature()
	{
		return new ModificationFeatureImpl();
	}
	public BindingFeature createBindingFeature()
	{
		return new BindingFeatureImpl();
	}

    public Provenance createProvenance()
    {
        return new ProvenanceImpl();
    }

    public SequenceSite createSequenceSite()
	{
		return new SequenceSiteImpl();
	}

	public SequenceInterval createSequenceInterval()
	{
		return new SequenceIntervalImpl();
	}

	public SequenceLocation createSequenceLocation()
	{
		return new SequenceLocationImpl();
	}

	public SmallMolecule createSmallMolecule()
	{
		return new SmallMoleculeImpl();
	}

    public Degradation createDegradation()
    {
        return new DegradationImpl();
    }
    
    public Stoichiometry createStoichiometry()
	{
		return new StoichiometryImpl();
	}

	public Transport createTransport()
	{
		return new TransportImpl();
	}

	public TransportWithBiochemicalReaction createTransportWithBiochemicalReaction()
	{
		return new TransportWithBiochemicalReactionImpl();
	}

	public UnificationXref createUnificationXref()
	{
		return new UnificationXrefImpl();
	}

    public TissueVocabulary createTissueVocabulary() {
        return new TissueVocabularyImpl();
    }

    public CellVocabulary createCellVocabulary() {
        return new CellVocabularyImpl();
    }

    public CellularLocationVocabulary createCellularLocationVocabulary() {
        return new CellularLocationVocabularyImpl();
    }

    public ExperimentalFormVocabulary createExperimentalFormVocabulary() {
        return new ExperimentalFormVocabularyImpl();
    }

    public EvidenceCodeVocabulary createEvidenceCodeVocabulary() {
        return new EvidenceCodeVocabularyImpl();
    }

    public EntityReferenceTypeVocabulary createEntityReferenceTypeVocabulary() {
        return new EntityReferenceTypeVocabularyImpl();
    }

    public SequenceRegionVocabulary createSequenceRegionVocabulary() {
        return new SequenceRegionVocabularyImpl();
    }

    public SequenceModificationVocabulary createSequenceModificationVocabulary() {
        return new SequenceModificationVocabularyImpl();
    }

    public  RelationshipTypeVocabulary createRelationshipTypeVocabulary() {
        return new  RelationshipTypeVocabularyImpl();
    }

    public CovalentBindingFeature createDisulfideFeature() {
        return new CovalentBindingFeatureImpl();
    }

    public FragmentFeature createFragmentFeature() {
        return new FragmentFeatureImpl();
    }

    public TemplateReaction createTemplateReaction() {
        return new TemplateReactionImpl();
    }

    public TemplateReactionRegulation createTemplateReactionRegulation() {
        return new TemplateReactionRegulationImpl();
    }

    public DnaRegion createDnaRegion() {
        return new DnaRegionImpl();
    }

    public DnaRegionReference createDnaRegionReference() {
        return new DnaRegionReferenceImpl();
    }

    public RnaRegion createRnaRegion() {
        return new RnaRegionImpl();
    }

    public RnaRegionReference createRnaRegionReference() {
        return new RnaRegionReferenceImpl();
    }
    
    public InteractionVocabulary createInteractionVocabulary() {
    	return new InteractionVocabularyImpl();
    }
    
    public PhenotypeVocabulary createPhenotypeVocabulary() {
    	return new PhenotypeVocabularyImpl();
    }

    public CovalentBindingFeature createCovalentBindingFeature() {
        return new CovalentBindingFeatureImpl();
    }
    
    public GeneticInteraction createGeneticInteraction() {
    	return new GeneticInteractionImpl();
    }
}
