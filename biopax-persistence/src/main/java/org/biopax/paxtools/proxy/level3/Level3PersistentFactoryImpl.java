package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.impl.BioPAXFactoryImpl;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.level3.*;

/**
 * User: root Date: Apr 26, 2006 Time: 3:06:08 PM_DOT
 */
public class Level3PersistentFactoryImpl extends BioPAXFactoryImpl implements Level3Factory
{
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXFactory ---------------------

	public BioPAXLevel getLevel()
	{
		return BioPAXLevel.L3;
	}

	public BioSource createBioSource()
	{
		return new BioSourceProxy();
	}

	public BiochemicalReaction createBiochemicalReaction()
	{
		return new BiochemicalReactionProxy();
	}

	public Catalysis createCatalysis()
	{
		return new CatalysisProxy();
	}

	public ChemicalStructure createChemicalStructure()
	{
		return new ChemicalStructureProxy();
	}

	public Complex createComplex()
	{
		return new ComplexProxy();
	}

	public ComplexAssembly createComplexAssembly()
	{
		return new ComplexAssemblyProxy();
	}


	public Interaction createInteraction()
	{
		return new InteractionProxy();
	}

	public MolecularInteraction createMolecularInteraction()
	{
		return new MolecularInteractionProxy();
	}

	public Score createScore()
	{
		return new ScoreProxy();
	}

	public Control createControl()
	{
		return new ControlProxy();
	}

	public Conversion createConversion()
	{
		return new ConversionProxy();
	}



	public DeltaG createDeltaG()
	{
		return new DeltaGProxy();
	}

	public BiochemicalPathwayStep createBiochemicalPathwayStep()
	{
		return new BiochemicalPathwayStepProxy();
	}

	public Dna createDna()
	{
		return new DnaProxy();
	}

	public EntityFeature createEntityFeature()
	{
		return new EntityFeatureProxy();
	}

	public Evidence createEvidence()
	{
		return new EvidenceProxy();
	}

	public ExperimentalForm createExperimentalForm()
	{
		return new ExperimentalFormProxy();
	}

    public Gene createGene()
    {
        return new GeneProxy();
    }

    public KPrime createKPrime()
	{
		return new KPrimeProxy();
	}

	public Modulation createModulation()
	{
		return new ModulationProxy();
	}

	public ControlledVocabulary createControlledVocabulary()
	{
		return new ControlledVocabularyProxy();
	}

	public Pathway createPathway()
	{
		return new PathwayProxy();
	}

	public PathwayStep createPathwayStep()
	{
		return new PathwayStepProxy();
	}

	public PhysicalEntity createPhysicalEntity()
	{
		return new PhysicalEntityProxy();
	}


	public Protein createProtein()
	{
		return new ProteinProxy();
	}

	public PublicationXref createPublicationXref()
	{
		return new PublicationXrefProxy();
	}


	public DnaReference createDnaReference()
	{
		return new DnaReferenceProxy();
	}

	public EntityReference createEntityReference()
	{
		return new EntityReferenceProxy();
	}



    public RnaReference createRnaReference()
	{
		return new RnaReferenceProxy();
	}

	public SmallMoleculeReference createSmallMoleculeReference()
	{
		return new SmallMoleculeReferenceProxy();
	}

	public ProteinReference createProteinReference()
	{
		return new ProteinReferenceProxy();
	}

	public RelationshipXref createRelationshipXref()
	{
		return new RelationshipXrefProxy();
	}

	public Rna createRna()
	{
		return new RnaProxy();
	}

	public ModificationFeature createModificationFeature()
	{
		return new ModificationFeatureProxy();
	}
	public BindingFeature createBindingFeature()
	{
		return new BindingFeatureProxy();
	}

    public Provenance createProvenance()
    {
        return new ProvenanceProxy();
    }

    public SequenceSite createSequenceSite()
	{
		return new SequenceSiteProxy();
	}

	public SequenceInterval createSequenceInterval()
	{
		return new SequenceIntervalProxy();
	}

	public SequenceLocation createSequenceLocation()
	{
		return new SequenceLocationProxy();
	}

	public SmallMolecule createSmallMolecule()
	{
		return new SmallMoleculeProxy();
	}

    public Degradation createDegradation()
    {
        return new DegradationProxy();
    }

    public Stoichiometry createStoichiometry()
	{
		return new StoichiometryProxy();
	}

	public Transport createTransport()
	{
		return new TransportProxy();
	}

	public TransportWithBiochemicalReaction createTransportWithBiochemicalReaction()
	{
		return new TransportWithBiochemicalReactionProxy();
	}

	public UnificationXref createUnificationXref()
	{
		return new UnificationXrefProxy();
	}

    public TissueVocabulary createTissueVocabulary() {
        return new TissueVocabularyProxy();
    }

    public CellVocabulary createCellVocabulary() {
        return new CellVocabularyProxy();
    }

    public CellularLocationVocabulary createCellularLocationVocabulary() {
        return new CellularLocationVocabularyProxy();
    }

    public ExperimentalFormVocabulary createExperimentalFormVocabulary() {
        return new ExperimentalFormVocabularyProxy();
    }

    public EvidenceCodeVocabulary createEvidenceCodeVocabulary() {
        return new EvidenceCodeVocabularyProxy();
    }

    public EntityReferenceTypeVocabulary createEntityReferenceTypeVocabulary() {
        return new EntityReferenceTypeVocabularyProxy();
    }

    public SequenceRegionVocabulary createSequenceRegionVocabulary() {
        return new SequenceRegionVocabularyProxy();
    }

    public SequenceModificationVocabulary createSequenceModificationVocabulary() {
        return new SequenceModificationVocabularyProxy();
    }

    public  RelationshipTypeVocabulary createRelationshipTypeVocabulary() {
        return new  RelationshipTypeVocabularyProxy();
    }

    public CovalentBindingFeature createDisulfideFeature() {
        return new CovalentBindingFeatureProxy();
    }

    public FragmentFeature createFragmentFeature() {
        return new FragmentFeatureProxy();
    }

    public TemplateReaction createTemplateReaction() {
        return new TemplateReactionProxy();
    }

    public TemplateReactionRegulation createTemplateReactionRegulation() {
        return new TemplateReactionRegulationProxy();
    }

	public CovalentBindingFeature createCovalentBindingFeature() {
		return new CovalentBindingFeatureProxy();
	}

	public DnaRegion createDnaRegion() {
		return new DnaRegionProxy();
	}

	public DnaRegionReference createDnaRegionReference() {
		return new DnaRegionReferenceProxy();
	}

	public InteractionVocabulary createInteractionVocabulary() {
		return null;
	}

	public PhenotypeVocabulary createPhenotypeVocabulary() {
		return new PhenotypeVocabularyProxy();
	}

	public RnaRegion createRnaRegion() {
		return new RnaRegionProxy();
	}

	public RnaRegionReference createRnaRegionReference() {
		return new RnaRegionReferenceProxy();
	}

    public GeneticInteraction createGeneticInteraction() {
    	return new GeneticInteractionProxy();
    }
    
}