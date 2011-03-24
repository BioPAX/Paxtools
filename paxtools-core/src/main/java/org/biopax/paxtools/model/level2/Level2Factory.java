package org.biopax.paxtools.model.level2;

import org.biopax.paxtools.model.BioPAXFactory;


public interface Level2Factory extends BioPAXFactory
{
// -------------------------- OTHER METHODS --------------------------

	bioSource createBioSource();

	biochemicalReaction createBiochemicalReaction();

	catalysis createCatalysis();

	chemicalStructure createChemicalStructure();

	complex createComplex();

	complexAssembly createComplexAssembly();

	confidence createConfidence();

	control createControl();

	conversion createConversion();

	dataSource createDataSource();

	deltaGprimeO createDeltaGprimeO();

	dna createDna();

	evidence createEvidence();

	experimentalForm createExperimentalForm();

	interaction createInteraction();

	kPrime createKPrime();

	modulation createModulation();

	openControlledVocabulary createOpenControlledVocabulary();

	pathway createPathway();

	pathwayStep createPathwayStep();

	physicalEntity createPhysicalEntity();

	physicalEntityParticipant createPhysicalEntityParticipant();

	physicalInteraction createPhysicalInteraction();

	protein createProtein();

	publicationXref createPublicationXref();

	relationshipXref createRelationshipXref();

	rna createRna();

	sequenceFeature createSequenceFeature();

	sequenceInterval createSequenceInterval();

	sequenceParticipant createSequenceParticipant();

	sequenceSite createSequenceSite();

	smallMolecule createSmallMolecule();

	transport createTransport();

	transportWithBiochemicalReaction createTransportWithBiochemicalReaction();

	unificationXref createUnificationXref();
}
