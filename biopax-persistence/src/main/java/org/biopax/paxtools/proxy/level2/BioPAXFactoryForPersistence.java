package org.biopax.paxtools.proxy.level2;

import java.lang.reflect.Method;
//import org.biopax.paxtools.impl.ModelImpl;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.*;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;
import org.biopax.paxtools.proxy.ModelForPersistence;

/**
 * User: root Date: Apr 26, 2006 Time: 3:06:08 PM_DOT
 */
public class BioPAXFactoryForPersistence /*extends BioPAXFactoryImpl*/ implements BioPAXFactory, Level2Factory 
{
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXFactory ---------------------
	
	public BioPAXLevel getLevel() {
		return BioPAXLevel.L2;
	}

	public Model createModel() {
		return new ModelForPersistence(this);
		//return new ModelImpl(this);
	}

	public BioPAXElement reflectivelyCreate(String name) {
		Method m = null;
		String methodName = "create" + name.substring(0, 1).toUpperCase() + name.substring(1);
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

//	public BioPAXElement reflectivelyCreate(Class<? extends BioPAXElement> c) {
//		return reflectivelyCreate(c.getSimpleName());
//	}

	public <T extends BioPAXElement> T reflectivelyCreate(Class<T> c) {
		return (T)reflectivelyCreate(c.getSimpleName());
	}

	public boolean canInstantiate(String name) {
		Method m = null;
		String methodName = "create" + name.substring(0, 1).toUpperCase() + name.substring(1);
		try {
			m = BioPAXFactoryForPersistence.class.getMethod(methodName);
		}
		catch (Exception e) {
		}
		return m != null;
	}

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
