package org.biopax.paxtools.proxy.level3;

import java.lang.reflect.Method;
//import org.biopax.paxtools.impl.ModelImpl;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;
import org.biopax.paxtools.proxy.ModelForPersistence;

/**
 * BioPAXFactoryForPersistence
 */
public class BioPAXFactoryForPersistence implements BioPAXFactory, Level3Factory
{
// BioPAXFactory

	public BioPAXLevel getLevel() {
		return BioPAXLevel.L3;
	}

	public Model createModel() {
		return new ModelForPersistence(this);
	}

	public BioPAXElement reflectivelyCreate(String name) {
		Method m = null;
		//String methodName = "create" + name.substring(0, 1).toUpperCase() + name.substring(1);
		String methodName = "create" + name;
		try {
			m = BioPAXFactoryForPersistence.class.getMethod(methodName);
		}
		catch (Exception e) {
		}
		if (m == null) {
			throw new IllegalBioPAXArgumentException("No creation methods for name: " + name);
		}
		try {
			return (BioPAXElement)m.invoke(this);
		}
		catch (Exception e) {
			throw new IllegalBioPAXArgumentException(e);
		}
	}


	public <T extends BioPAXElement> T reflectivelyCreate(Class<T> c) {
		return (T)reflectivelyCreate(c.getSimpleName());
	}

	public boolean canInstantiate(String name) {
		Method m = null;
		//String methodName = "create" + name.substring(0, 1).toUpperCase() + name.substring(1);
		String methodName = "create" + name;
		try {
			m = BioPAXFactoryForPersistence.class.getMethod(methodName);
		}
		catch (Exception e) {
		}
		return m != null;
	}

// Level3Factory

	public BiochemicalReaction createBiochemicalReaction() {
		return new BiochemicalReactionProxy();
	}

	public BioSource createBioSource() {
		return new BioSourceProxy();
	}

	public Catalysis createCatalysis() {
		return new CatalysisProxy();
	}

	public ChemicalStructure createChemicalStructure() {
		return new ChemicalStructureProxy();
	}

	public Complex createComplex() {
		return new ComplexProxy();
	}

	public ComplexAssembly createComplexAssembly() {
		return new ComplexAssemblyProxy();
	}

	public Degradation createDegradation() {
		return new DegradationProxy();
	}

    public Stoichiometry createStoichiometry() {
		return new StoichiometryProxy();
    }

	public Score createScore() {
		return new ScoreProxy();
	}

	public Control createControl() {
		return new ControlProxy();
	}

	public Conversion createConversion() {
		return new ConversionProxy();
	}

	public ModificationFeature createModificationFeature() {
		return new ModificationFeatureProxy();
	}

	public BindingFeature createBindingFeature() {
		return new BindingFeatureProxy();
	}

	public Provenance createProvenance() {
		return new ProvenanceProxy();
	}


	public DeltaG createDeltaG() {
		return new DeltaGProxy();
	}

	public BiochemicalPathwayStep createBiochemicalPathwayStep() {
		return new BiochemicalPathwayStepProxy();
	}

	public Dna createDna() {
		return new DnaProxy();
	}

	public EntityFeature createEntityFeature() {
		return new EntityFeatureProxy();
	}

	public Evidence createEvidence() {
		return new EvidenceProxy();
	}

	public ExperimentalForm createExperimentalForm() {
		return new ExperimentalFormProxy();
	}

    public Gene createGene() {
		return new GeneProxy();
    }

	public Interaction createInteraction() {
		return new InteractionProxy();
	}

	public KPrime createKPrime() {
		return new KPrimeProxy();
	}

	public Modulation createModulation() {
		return new ModulationProxy();
	}

	public ControlledVocabulary createControlledVocabulary() {
		return new ControlledVocabularyProxy();
	}

	public Pathway createPathway() {
		return new PathwayProxy();
	}

	public PathwayStep createPathwayStep() {
		return new PathwayStepProxy();
	}

	public PhysicalEntity createPhysicalEntity() {
		return new PhysicalEntityProxy();
	}

	public MolecularInteraction createMolecularInteraction() {
		return new MolecularInteractionProxy();
	}

	public Protein createProtein() {
		return new ProteinProxy();
	}

	public PublicationXref createPublicationXref() {
		return new PublicationXrefProxy();
	}

	public EntityReference createEntityReference() {
		return new EntityReferenceProxy();
	}

	public DnaReference createDnaReference() {
		return new DnaReferenceProxy();
	}

	public RnaReference createRnaReference() {
		return new RnaReferenceProxy();
	}

	public SmallMoleculeReference createSmallMoleculeReference() {
		return new SmallMoleculeReferenceProxy();
	}

	public ProteinReference createProteinReference() {
		return new ProteinReferenceProxy();
	}

	public RelationshipXref createRelationshipXref() {
		return new RelationshipXrefProxy();
	}

	public Rna createRna() {
		return new RnaProxy();
	}

	public SequenceInterval createSequenceInterval() {
		return new SequenceIntervalProxy();
	}

	public SequenceLocation createSequenceLocation() {
		return new SequenceLocationProxy();
	}

	public SequenceSite createSequenceSite() {
		return new SequenceSiteProxy();
	}

	public SmallMolecule createSmallMolecule() {
		return new SmallMoleculeProxy();
	}

	public Transport createTransport() {
		return new TransportProxy();
	}

	public TransportWithBiochemicalReaction createTransportWithBiochemicalReaction() {
		return new TransportWithBiochemicalReactionProxy();
	}

	public UnificationXref createUnificationXref() {
		return new UnificationXrefProxy();
	}

	public TemplateReaction createTemplateReaction() {
		return new TemplateReactionProxy();
	}

    public TemplateReactionRegulation createTemplateReactionRegulation() {
        return new TemplateReactionRegulationProxy();
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

    public RelationshipTypeVocabulary createRelationshipTypeVocabulary() {
         return new RelationshipTypeVocabularyProxy();
    }

    public CovalentBindingFeature createDisulfideFeature() {
        return new CovalentBindingFeatureProxy();
    }

    public FragmentFeature createFragmentFeature() {
        return new FragmentFeatureProxy();
    }

    public DnaRegion createDnaRegion() {
        return new DnaRegionProxy();
    }

    public DnaRegionReference createDnaRegionReference() {
        return new DnaRegionReferenceProxy();
    }

    public RnaRegion createRnaRegion() {
        return new RnaRegionProxy();
    }

    public RnaRegionReference createRnaRegionReference() {
        return new RnaRegionReferenceProxy();
    }

    public InteractionVocabulary createInteractionVocabulary() {
    	return new InteractionVocabularyProxy();
    }
    
    public PhenotypeVocabulary createPhenotypeVocabulary() {
    	return new PhenotypeVocabularyProxy();
    }

	public CovalentBindingFeature createCovalentBindingFeature() {
    	return new CovalentBindingFeatureProxy();
	}

    public GeneticInteraction createGeneticInteraction() {
    	return new GeneticInteractionProxy();
    }
	
}
