package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.impl.BioPAXFactoryImpl;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.level2.*;

/**
 * User: root Date: Apr 26, 2006 Time: 3:06:08 PM_DOT
 * 
 * @deprecated use org.biopax.paxtools.proxy.level2.BioPAXFactoryForPersistence instead [rodche, Mar 25, 2010]
 */
public class Level2PersistentFactoryImpl extends BioPAXFactoryImpl implements Level2Factory
{
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXFactory ---------------------



	public bioSource createBioSource()
	{
		return new bioSourceProxy();
	}

	public biochemicalReaction createBiochemicalReaction()
	{
		return new biochemicalReactionProxy();
	}

	public catalysis createCatalysis()
	{
		return new catalysisProxy();
	}

	public chemicalStructure createChemicalStructure()
	{
		return new chemicalStructureProxy();
	}

	public complex createComplex()
	{
		return new complexProxy();
	}

	public complexAssembly createComplexAssembly()
	{
		return new complexAssemblyProxy();
	}

	public interaction createInteraction()
	{
		return new interactionProxy();
	}

	public physicalInteraction createPhysicalInteraction()
	{
		return new physicalInteractionProxy();
	}

	public confidence createConfidence()
	{
		return new confidenceProxy();
	}

	public control createControl()
	{
		return new controlProxy();
	}

	public conversion createConversion()
	{
		return new conversionProxy();
	}

	public dataSource createDataSource()
	{
		return new dataSourceProxy();
	}

	public deltaGprimeO createDeltaGprimeO()
	{
		return new deltaGprimeOProxy();
	}

	public dna createDna()
	{
		return new dnaProxy();
	}

	public evidence createEvidence()
	{
		return new evidenceProxy();
	}

	public experimentalForm createExperimentalForm()
	{
		return new experimentalFormProxy();
	}

	public kPrime createKPrime()
	{
		return new kPrimeProxy();
	}


	public BioPAXLevel getLevel()
	{
		return BioPAXLevel.L2;
	}


	public modulation createModulation()
	{
		return new modulationProxy();
	}

	public openControlledVocabulary createOpenControlledVocabulary()
	{
		return new openControlledVocabularyProxy();
	}

	public pathway createPathway()
	{
		return new pathwayProxy();
	}

	public pathwayStep createPathwayStep()
	{
		return new pathwayStepProxy();
	}

	public physicalEntity createPhysicalEntity()
	{
		return new physicalEntityProxy();
	}

	public physicalEntityParticipant createPhysicalEntityParticipant()
	{
		return new physicalEntityParticipantProxy();
	}

	public protein createProtein()
	{
		return new proteinProxy();
	}

	public publicationXref createPublicationXref()
	{
		return new publicationXrefProxy();
	}

	public relationshipXref createRelationshipXref()
	{
		return new relationshipXrefProxy();
	}

	public rna createRna()
	{
		return new rnaProxy();
	}

	public sequenceFeature createSequenceFeature()
	{
		return new sequenceFeatureProxy();
	}

	public sequenceParticipant createSequenceParticipant()
	{
		return new sequenceParticipantProxy();
	}

	public sequenceSite createSequenceSite()
	{
		return new sequenceSiteProxy();
	}

	public sequenceInterval createSequenceInterval()
	{
		return new sequenceIntervalProxy();
	}

	public smallMolecule createSmallMolecule()
	{
		return new smallMoleculeProxy();
	}

	public transport createTransport()
	{
		return new transportProxy();
	}

	public transportWithBiochemicalReaction createTransportWithBiochemicalReaction()
	{
		return new transportWithBiochemicalReactionProxy();
	}

	public unificationXref createUnificationXref()
	{
		return new unificationXrefProxy();
	}
}