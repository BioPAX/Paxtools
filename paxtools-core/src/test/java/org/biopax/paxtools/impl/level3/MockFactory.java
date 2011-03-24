package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.controller.*;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.*;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.lang.reflect.*;
import java.util.*;

/**
 * This factory returns decorated objects for testing.
 */
public class MockFactory implements Level3Factory {
// ------------------------------ FIELDS ------------------------------

    private static final List<String> strings = Arrays
            .asList(" ",
                    "alpha",
                    "beta",
                    "gamma",
                    "_~/-\\\t\b,",
                    "\udddd\ucccc\uaaaa\ubbbb");
    private static final List<Float> floats =
            Arrays.asList(Float.MAX_VALUE, 1.0F, 0.0F, Float.MIN_VALUE);
    private static final List<Double> doubles =
            Arrays.asList(Double.MAX_VALUE, 1.0, 0.0, Double.MIN_VALUE);
    private static final List<Integer> ints =
            Arrays.asList(Integer.MAX_VALUE, 1, 0, Integer.MIN_VALUE + 1);
    private static final List<Boolean> booleans =
            Arrays.asList(Boolean.TRUE,Boolean.FALSE);
    private final Level3Factory factory;


    private final EditorMap map;

// --------------------------- CONSTRUCTORS ---------------------------

    public MockFactory() {
        this(new Level3FactoryImpl());
    }

    public MockFactory(Level3Factory factory) {
        this.factory = factory;
        map = null; //new SimpleEditorMap(BioPAXLevel.L3);
     // TODO cannot use SimpleEditorMap here (from paxtools-simple-io, creates a dependency loop)
        throw new UnsupportedOperationException("Not implemented: cannot use SimpleEditorMap here" +
        	" (from paxtools-simple-io, creates a dependency loop)!");
    }

// --------------------- GETTER / SETTER METHODS ---------------------

    public EditorMap getMap() {
        return map;
    }

// ------------------------ INTERFACE METHODS ------------------------

// --------------------- Interface BioPAXFactory ---------------------

    public BioSource createBioSource() {
        BioSource bpe = factory.createBioSource();
        populateMock(bpe);
        return bpe;
    }

    public BiochemicalReaction createBiochemicalReaction() {
        BiochemicalReaction bpe = factory.createBiochemicalReaction();
        populateMock(bpe);
        return bpe;
    }

    public Catalysis createCatalysis() {
        Catalysis bpe = factory.createCatalysis();
        populateMock(bpe);
        return bpe;
    }

    public ChemicalStructure createChemicalStructure() {
        ChemicalStructure bpe = factory.createChemicalStructure();
        populateMock(bpe);
        return bpe;
    }

    public Complex createComplex() {
        Complex bpe = factory.createComplex();
        populateMock(bpe);
        return bpe;
    }

    public ComplexAssembly createComplexAssembly() {
        ComplexAssembly bpe = factory.createComplexAssembly();
        populateMock(bpe);
        return bpe;
    }

    public Degradation createDegradation() {
        Degradation bpe = factory.createDegradation();
        populateMock(bpe);
        return bpe;
    }

    public Interaction createInteraction() {
        Interaction bpe = factory.createInteraction();
        populateMock(bpe);
        return bpe;
    }

    public MolecularInteraction createMolecularInteraction() {
        MolecularInteraction bpe = factory.createMolecularInteraction();
        populateMock(bpe);
        return bpe;
    }

    public Score createScore() {
        Score bpe = factory.createScore();
        populateMock(bpe);
        return bpe;
    }

    public Control createControl() {
        Control bpe = factory.createControl();
        populateMock(bpe);
        return bpe;
    }

    public Conversion createConversion() {
        Conversion bpe = factory.createConversion();
        populateMock(bpe);
        return bpe;
    }

    public ModificationFeature createModificationFeature() {
        ModificationFeature bpe = factory.createModificationFeature();
        populateMock(bpe);
        return bpe;
    }

    public BindingFeature createBindingFeature() {
        BindingFeature bpe = factory.createBindingFeature();
        populateMock(bpe);
        return bpe;
    }

    public CovalentBindingFeature createCovalentBindingFeature() {
        CovalentBindingFeature bpe = factory.createCovalentBindingFeature();
        populateMock(bpe);
        return bpe;
    }

    public GeneticInteraction createGeneticInteraction() {
      GeneticInteraction bpe = factory.createGeneticInteraction();
        populateMock(bpe);
        return bpe;
    }

    public Provenance createProvenance() {
        Provenance bpe = factory.createProvenance();
        populateMock(bpe);
        return bpe;
    }

    public DeltaG createDeltaG() {
        DeltaG bpe = factory.createDeltaG();
        populateMock(bpe);
        return bpe;
    }

    public BiochemicalPathwayStep createBiochemicalPathwayStep() {
        BiochemicalPathwayStep bpe = factory.createBiochemicalPathwayStep();
        populateMock(bpe);
        return bpe;

    }

    public Dna createDna() {
        Dna bpe = factory.createDna();
        populateMock(bpe);
        return bpe;
    }

    public EntityFeature createEntityFeature() {
        EntityFeature bpe = factory.createEntityFeature();
        populateMock(bpe);
        return bpe;

    }

    public Evidence createEvidence() {
        Evidence bpe = factory.createEvidence();
        populateMock(bpe);
        return bpe;
    }

    public ExperimentalForm createExperimentalForm() {
        ExperimentalForm bpe = factory.createExperimentalForm();
        populateMock(bpe);
        return bpe;
    }

    public Gene createGene() {
        Gene bpe = factory.createGene();
        populateMock(bpe);
        return bpe;
    }

    public KPrime createKPrime() {
        KPrime bpe = factory.createKPrime();
        populateMock(bpe);
        return bpe;
    }


    public boolean canInstantiate(String name) {
        return factory.canInstantiate(name);
    }

    public BioPAXLevel getLevel() {
        return factory.getLevel();
    }

    public Model createModel() {
        return factory.createModel();
    }

    public BioPAXElement reflectivelyCreate(String name, String uri) {
        return factory.reflectivelyCreate(name, uri);
    }

    public Modulation createModulation() {
        Modulation bpe = factory.createModulation();
        populateMock(bpe);
        return bpe;
    }

    public ControlledVocabulary createControlledVocabulary() {
        ControlledVocabulary bpe = factory.createControlledVocabulary();
        populateMock(bpe);
        return bpe;
    }

    public Pathway createPathway() {
        Pathway bpe = factory.createPathway();
        populateMock(bpe);
        return bpe;
    }

    public PathwayStep createPathwayStep() {
        PathwayStep bpe = factory.createPathwayStep();
        populateMock(bpe);
        return bpe;
    }

    public PhysicalEntity createPhysicalEntity() {
        PhysicalEntity bpe = factory.createPhysicalEntity();
        populateMock(bpe);
        return bpe;
    }


    public Protein createProtein() {
        Protein bpe = factory.createProtein();
        populateMock(bpe);
        return bpe;
    }

    public PublicationXref createPublicationXref() {
        PublicationXref bpe = factory.createPublicationXref();
        populateMock(bpe);
        return bpe;
    }



    public DnaReference createDnaReference() {
        DnaReference bpe = factory.createDnaReference();
        populateMock(bpe);
        return bpe;

    }

    public RnaReference createRnaReference() {
        RnaReference bpe = factory.createRnaReference();
        populateMock(bpe);
        return bpe;
    }

    public SmallMoleculeReference createSmallMoleculeReference() {
        SmallMoleculeReference bpe = factory.createSmallMoleculeReference();
        populateMock(bpe);
        return bpe;

    }

    public ProteinReference createProteinReference() {
        ProteinReference bpe = factory.createProteinReference();
        populateMock(bpe);
        return bpe;

    }

    public RelationshipXref createRelationshipXref() {
        RelationshipXref bpe = factory.createRelationshipXref();
        populateMock(bpe);
        return bpe;
    }

    public Rna createRna() {
        Rna bpe = factory.createRna();
        populateMock(bpe);
        return bpe;
    }

    public SequenceSite createSequenceSite() {
        SequenceSite bpe = factory.createSequenceSite();
        populateMock(bpe);
        return bpe;
    }

    public SequenceInterval createSequenceInterval() {
        SequenceInterval bpe = factory.createSequenceInterval();
        populateMock(bpe);
        return bpe;
    }

    public SequenceLocation createSequenceLocation() {
        SequenceLocation bpe = factory.createSequenceLocation();
        populateMock(bpe);
        return bpe;
    }

    public SmallMolecule createSmallMolecule() {
        SmallMolecule bpe = factory.createSmallMolecule();
        populateMock(bpe);
        return bpe;
    }

    public Stoichiometry createStoichiometry() {
    	Stoichiometry bpe = factory.createStoichiometry();
    	populateMock(bpe);
        return bpe;
    }

    public Transport createTransport() {
        Transport bpe = factory.createTransport();
        populateMock(bpe);
        return bpe;
    }

    public TransportWithBiochemicalReaction createTransportWithBiochemicalReaction() {
        TransportWithBiochemicalReaction bpe =
                factory.createTransportWithBiochemicalReaction();
        populateMock(bpe);
        return bpe;
    }

    public UnificationXref createUnificationXref() {
        UnificationXref bpe = factory.createUnificationXref();
        populateMock(bpe);
        bpe.setId(((int) Math.random() * Integer.MAX_VALUE) + "");
        return bpe;
    }

    public TissueVocabulary createTissueVocabulary() {
        TissueVocabulary bpe = factory.createTissueVocabulary();
        populateMock(bpe);
        return bpe;

    }

    public CellVocabulary createCellVocabulary() {
        CellVocabulary bpe = factory.createCellVocabulary();
        populateMock(bpe);
        return bpe;

    }

    public CellularLocationVocabulary createCellularLocationVocabulary() {
        CellularLocationVocabulary bpe = factory.createCellularLocationVocabulary();
        populateMock(bpe);
        return bpe;

    }

    public ExperimentalFormVocabulary createExperimentalFormVocabulary() {
        ExperimentalFormVocabulary bpe = factory.createExperimentalFormVocabulary();
        populateMock(bpe);
        return bpe;

    }

    public EvidenceCodeVocabulary createEvidenceCodeVocabulary() {
        EvidenceCodeVocabulary bpe = factory.createEvidenceCodeVocabulary();
        populateMock(bpe);
        return bpe;

    }


    public SequenceRegionVocabulary createSequenceRegionVocabulary() {
        SequenceRegionVocabulary bpe = factory.createSequenceRegionVocabulary();
        populateMock(bpe);
        return bpe;

    }

    public SequenceModificationVocabulary createSequenceModificationVocabulary() {
        SequenceModificationVocabulary bpe = factory.createSequenceModificationVocabulary();
        populateMock(bpe);
        return bpe;
    }

    public RelationshipTypeVocabulary createRelationshipTypeVocabulary() {
        RelationshipTypeVocabulary bpe = factory.createRelationshipTypeVocabulary();
        populateMock(bpe);
        return bpe;
    }

    public FragmentFeature createFragmentFeature() {
        FragmentFeature bpe = factory.createFragmentFeature();
        populateMock(bpe);
        return bpe;
    }

    public TemplateReaction createTemplateReaction() {
        TemplateReaction bpe = factory.createTemplateReaction();
        populateMock(bpe);
        return bpe;
    }

    public TemplateReactionRegulation createTemplateReactionRegulation() {
        TemplateReactionRegulation bpe = factory.createTemplateReactionRegulation();
        populateMock(bpe);
        return bpe;
    }

    public DnaRegion createDnaRegion() {
        DnaRegion bpe = factory.createDnaRegion();
        populateMock(bpe);
        return bpe;
    }

    public DnaRegionReference createDnaRegionReference() {
        DnaRegionReference bpe = factory.createDnaRegionReference();
        populateMock(bpe);
        return bpe;
    }

    public RnaRegion createRnaRegion() {
        RnaRegion bpe = factory.createRnaRegion();
        populateMock(bpe);
        return bpe;
    }

    public RnaRegionReference createRnaRegionReference() {
        RnaRegionReference bpe = factory.createRnaRegionReference();
        populateMock(bpe);
        return bpe;
    }

    public InteractionVocabulary createInteractionVocabulary()
    {
        InteractionVocabulary bpe = factory.createInteractionVocabulary();
        populateMock(bpe);
        return bpe;
    }

    public PhenotypeVocabulary createPhenotypeVocabulary() {
        PhenotypeVocabulary bpe = factory.createPhenotypeVocabulary();
        populateMock(bpe);
        return bpe;
    }

// -------------------------- OTHER METHODS --------------------------

    private void populateMock(BioPAXElement bpe) {
        Set<PropertyEditor> propertyEditors =
                map.getEditorsOf(bpe);

        for (PropertyEditor propertyEditor : propertyEditors) {
            boolean multiple = propertyEditor.isMultipleCardinality();
            Object value = null;
            if (propertyEditor instanceof StringPropertyEditor) {
                value = multiple ? strings : strings.get(4);
            } else {
                Class range = propertyEditor.getRange();
                if (propertyEditor instanceof PrimitivePropertyEditor) {
                    if (range == float.class) {
                        value = multiple ? floats : floats.get(1);
                    } else if (range == double.class) {
                        value = multiple ? doubles : doubles.get(1);
                    } else if (range == int.class) {
                        value = multiple ? ints : ints.get(1);
                    }
                    else if (range == Boolean.class) {
                        value = multiple ? booleans : booleans.get(1);
                    }
                } else if (propertyEditor instanceof EnumeratedPropertyEditor) {
                    Field[] fields = range.getFields();
                    if (multiple) {
                        value = new HashSet();
                    }
                    for (Field field : fields) {
                        if (field.isEnumConstant()) {
                            try {
                                if (multiple) {
                                    ((Set) value).add(field.get(bpe));
                                } else {
                                    value = field.get(bpe);
                                    break;
                                }
                            }
                            catch (IllegalAccessException e) {
                                throw new IllegalBioPAXArgumentException();
                            }
                        }
                    }
                } else {
                    if (!Entity.class.isAssignableFrom(range)) {
                        if (multiple) {
                            value =
                                    createRestrictedMock(propertyEditor, bpe, 3);
                        } else {
                            value = createRestrictedMock(propertyEditor, bpe, 1)
                                    .iterator().next();
                        }
                    }
                }
            }
            if (value != null) {
                if (multiple) {
                    Collection values = ((Collection) value);
                    if (!values.isEmpty()) {
                        Integer max =
                                propertyEditor.getMaxCardinality(
                                        bpe.getModelInterface());
                        values = upToMax(values, max);

                        for (Object o : values) {
                            propertyEditor.setValueToBean(o, bpe);
                        }
                    } else {
                        propertyEditor.setValueToBean(value, bpe);
                    }
                }
            }
        }
    }

    private HashSet<BioPAXElement> createRestrictedMock(PropertyEditor propertyEditor,
                                                        BioPAXElement bpe, int k) {
        HashSet<BioPAXElement> hashSet = new HashSet<BioPAXElement>();

        Object[] restricted =
                ((ObjectPropertyEditor) propertyEditor)
                        .getRestrictedRangesFor(bpe.getModelInterface())
                        .toArray();
        int length = restricted.length;
        for (int i = 0; i < k; i++) {
            Class restrictedRange = (Class) restricted[i % length];
            hashSet.add(createMock(restrictedRange, bpe.getClass()));
        }
        return hashSet;
    }

    private BioPAXElement createMock(Class toCreate, Class domain) {
        assert domain != null;
        Class actual;
        actual = findConcreteMockClass(toCreate, domain);
        if (actual != null) {
            return map.getLevel().getDefaultFactory()
            	.reflectivelyCreate(actual, null); // FIXME no RDFID ok?
        } else {
            System.out.println("actual = " + actual);
            System.out.println("toCreate = " + toCreate);
            return null;
        }
    }

    private Class findConcreteMockClass(Class toCreate, Class domain) {
        Class actual = null;
        if (map.getLevel().getDefaultFactory()
                .canInstantiate(toCreate.getSimpleName())
                &&
                !toCreate.isAssignableFrom(domain)) {
            actual = toCreate;
        } else {
            Set<Class> classesOf = map.getKnownSubClassesOf(toCreate);
            for (Class subclass : classesOf) {
                if (!subclass.isAssignableFrom(domain) &&
                        subclass != toCreate &&
                        subclass.getPackage().getName()
                                .startsWith("org.biopax.paxtools.model")) {
                    actual = findConcreteMockClass(subclass, domain);
                    break;
                }
            }
        }
        if(actual==null)
        {
            System.out.println("Failed to find restricted domain:" + domain +"for class " +toCreate +". " +
                    "This might be a bug or a self reference that might cause cycles");
        }
        return actual;
    }

    private Collection upToMax(Collection values, Integer max) {
        int size = values.size();
        if (max != null && max < size) {
            values = new ArrayList(values);
            for (int i = size - 1; i == max; i--) {
                ((List) values).remove(i);
            }
            assert values.size() == max;
        }
        return values;
    }

	public CovalentBindingFeature createDisulfideFeature() {
		CovalentBindingFeature bpe = factory.createDisulfideFeature();
		populateMock(bpe);
		return bpe;
	}

	public <T extends BioPAXElement> T reflectivelyCreate(Class<T> aClass, String uri) 
	{
		T bpe = reflectivelyCreate(aClass, uri);
		return bpe;
	}
}