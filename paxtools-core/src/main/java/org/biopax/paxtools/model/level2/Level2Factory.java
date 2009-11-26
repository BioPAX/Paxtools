package org.biopax.paxtools.model.level2;

import org.biopax.paxtools.model.BioPAXFactory;

/**
 */
public interface Level2Factory extends BioPAXFactory
{
// -------------------------- OTHER METHODS --------------------------

	public bioSource createBioSource();

	public biochemicalReaction createBiochemicalReaction();

	public catalysis createCatalysis();

	public chemicalStructure createChemicalStructure();

	public complex createComplex();

	public complexAssembly createComplexAssembly();

	public confidence createConfidence();

	public control createControl();

	public conversion createConversion();

	public dataSource createDataSource();

	public deltaGprimeO createDeltaGprimeO();

	public dna createDna();

	public evidence createEvidence();

	public experimentalForm createExperimentalForm();

	public interaction createInteraction();

	public kPrime createKPrime();


	public modulation createModulation();

	public openControlledVocabulary createOpenControlledVocabulary();

	public pathway createPathway();

	public pathwayStep createPathwayStep();

	public physicalEntity createPhysicalEntity();

	public physicalEntityParticipant createPhysicalEntityParticipant();

	public physicalInteraction createPhysicalInteraction();

	public protein createProtein();

	public publicationXref createPublicationXref();

	public relationshipXref createRelationshipXref();

	public rna createRna();

	public sequenceFeature createSequenceFeature();

	public sequenceInterval createSequenceInterval();

	public sequenceParticipant createSequenceParticipant();

	public sequenceSite createSequenceSite();

	public smallMolecule createSmallMolecule();

	public transport createTransport();

	public transportWithBiochemicalReaction createTransportWithBiochemicalReaction();

	public unificationXref createUnificationXref();
}
