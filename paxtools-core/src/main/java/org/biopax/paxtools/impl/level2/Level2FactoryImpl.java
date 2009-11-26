package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.impl.BioPAXFactoryImpl;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.level2.*;

/**
 * User: root Date: Apr 26, 2006 Time: 3:06:08 PM_DOT
 */
public class Level2FactoryImpl extends BioPAXFactoryImpl implements Level2Factory
{
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXFactory ---------------------



	public bioSource createBioSource()
	{
		return new bioSourceImpl();
	}

	public biochemicalReaction createBiochemicalReaction()
	{
		return new biochemicalReactionImpl();
	}

	public catalysis createCatalysis()
	{
		return new catalysisImpl();
	}

	public chemicalStructure createChemicalStructure()
	{
		return new chemicalStructureImpl();
	}

	public complex createComplex()
	{
		return new complexImpl();
	}

	public complexAssembly createComplexAssembly()
	{
		return new complexAssemblyImpl();
	}

	public interaction createInteraction()
	{
		return new interactionImpl();
	}

	public physicalInteraction createPhysicalInteraction()
	{
		return new physicalInteractionImpl();
	}

	public confidence createConfidence()
	{
		return new confidenceImpl();
	}

	public control createControl()
	{
		return new controlImpl();
	}

	public conversion createConversion()
	{
		return new conversionImpl();
	}

	public dataSource createDataSource()
	{
		return new dataSourceImpl();
	}

	public deltaGprimeO createDeltaGprimeO()
	{
		return new deltaGprimeOImpl();
	}

	public dna createDna()
	{
		return new dnaImpl();
	}

	public evidence createEvidence()
	{
		return new evidenceImpl();
	}

	public experimentalForm createExperimentalForm()
	{
		return new experimentalFormImpl();
	}

	public kPrime createKPrime()
	{
		return new kPrimeImpl();
	}


	public BioPAXLevel getLevel()
	{
		return BioPAXLevel.L2;
	}


	public modulation createModulation()
	{
		return new modulationImpl();
	}

	public openControlledVocabulary createOpenControlledVocabulary()
	{
		return new openControlledVocabularyImpl();
	}

	public pathway createPathway()
	{
		return new pathwayImpl();
	}

	public pathwayStep createPathwayStep()
	{
		return new pathwayStepImpl();
	}

	public physicalEntity createPhysicalEntity()
	{
		return new physicalEntityImpl();
	}

	public physicalEntityParticipant createPhysicalEntityParticipant()
	{
		return new physicalEntityParticipantImpl();
	}

	public protein createProtein()
	{
		return new proteinImpl();
	}

	public publicationXref createPublicationXref()
	{
		return new publicationXrefImpl();
	}

	public relationshipXref createRelationshipXref()
	{
		return new relationshipXrefImpl();
	}

	public rna createRna()
	{
		return new rnaImpl();
	}

	public sequenceFeature createSequenceFeature()
	{
		return new sequenceFeatureImpl();
	}

	public sequenceParticipant createSequenceParticipant()
	{
		return new sequenceParticipantImpl();
	}

	public sequenceSite createSequenceSite()
	{
		return new sequenceSiteImpl();
	}

	public sequenceInterval createSequenceInterval()
	{
		return new sequenceIntervalImpl();
	}

	public smallMolecule createSmallMolecule()
	{
		return new smallMoleculeImpl();
	}

	public transport createTransport()
	{
		return new transportImpl();
	}

	public transportWithBiochemicalReaction createTransportWithBiochemicalReaction()
	{
		return new transportWithBiochemicalReactionImpl();
	}

	public unificationXref createUnificationXref()
	{
		return new unificationXrefImpl();
	}
}
