// $Id: TestMappingL2.java,v 1.2 2009/11/23 13:59:42 rodche Exp $
//------------------------------------------------------------------------------
/** Copyright (c) 2009 Memorial Sloan-Kettering Cancer Center.
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/
package org.mskcc.psibiopax.converter;

// imports

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.*;
import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.model.Entry;
import psidev.psi.mi.xml.model.EntrySet;

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * This tests that a PSI-MI document (level 2-compact) is correctly mapped into a biopax model (L2).
 *
 * @author Benjamin Gross
 */
public class TestMappingL2 extends TestCase implements BioPAXMarshaller {

	/**
	 * psi-mi test file
	 */
	private static final String PSI_MI_TEST_FILE = "10523676-compact.xml";

	/**
	 * Used for synchronization.
	 */
	private Model bpModel;

	/**
	 * synchronization object.
	 */
	private final Object syncObj = new Object();

    /**
     * Returns the description/name of the test.
	 * 
	 * @return String
     */
    public String getName() {
        return "TestMapping: Tests the proper mapping of a PSI-MI XML file (level 2-compact) to an in memory Paxtools Model";
    }

	/**
	 * Dynamically adds all methods as tests that begin with 'test'
	 */
    public static Test suite() {
        return new TestSuite(TestMappingL2.class);
    }

	/**
	 * The big deal main - if we want to run from command line.
	 */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

	/**
     * Tests that a PSI-MI document (level 2-compact) is correctly mapped into a biopax model.
	 */
    public void testMapping() {

		// open file
		try {

			// unmarshall the data, close the stream
			PsimiXmlReader reader = new PsimiXmlReader();
            InputStream is = getClass().getClassLoader().getResourceAsStream(PSI_MI_TEST_FILE);
			EntrySet es = reader.read(is);
			is.close();
			Collection<Entry> entries =  es.getEntries();
			
			// we should only have 1 entry
            Assert.assertEquals(entries.size(), 1);

			// create a biopax mapper
			BioPAXMapper bpMapper = new BioPAXMapperImp(BioPAXLevel.L2);
			bpMapper.setNamespace(EntryMapper.RDF_ID_PREFIX);

			// get entry
			Entry entry = (Entry)entries.iterator().next();
			EntryMapper mapper = new EntryMapper(bpMapper, this, entry, 1970);
			mapper.start();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		// infinite loop for a bit
		while (true) {

			// have all the entries completed ?
			synchronized(syncObj) {
				if (bpModel != null) {
					break;
				}
			}

			// sleep for a bit
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException e){
				e.printStackTrace();
				System.exit(1);
			}
		}

		// check Model returned from mapper process
		checkModel();
	}

	/**
	 * Method called when BioPAX data is ready to be marshalled
	 *
	 * @param bpModel Model
	 */
	public void addModel(Model bpModel) {

		// add model to our list,
		// increment number of entries
		synchronized (syncObj) {
			this.bpModel = bpModel;
		}
	}

	/**
	 * Checks the model(s) returned from the mapper.
	 */
	private void checkModel() {

		Iterator it;

		// get the element list
		Set<BioPAXElement> biopaxElements = bpModel.getObjects();
		Assert.assertEquals(145, biopaxElements.size());

		// get the element
		BioPAXElement bpElement = null;
		for (BioPAXElement biopaxElement : biopaxElements) {
			if (biopaxElement.getRDFId().equals("HTTP://PATHWAYCOMMONS.ORG/PSI2BP#_3323281832130706316")) {
				bpElement = biopaxElement;
				break;
			}
		}
		Assert.assertTrue(bpElement != null);

		// cast to interaction
		physicalInteraction bpInteraction = (physicalInteraction)bpElement;

		// participants
		Set<InteractionParticipant> interactionParticipants = bpInteraction.getPARTICIPANTS();
		Assert.assertEquals(2, interactionParticipants.size());

		// get participant
		sequenceParticipant participant = null;
		for (InteractionParticipant interactionParticipant : interactionParticipants) {
			if (interactionParticipant.getRDFId().equals("HTTP://PATHWAYCOMMONS.ORG/PSI2BP#_5767007948877890882")) {
				participant = (sequenceParticipant)interactionParticipant;
			}
		}
		Assert.assertTrue(participant != null);

		// check sequence feature list/sequence feature
		checkSequenceFeatures(participant);
		
		// physical entity
		checkPhysicalEntity(participant);

	}

	/**
	 * Checks sequence feature list of given sequence participant.
	 *
	 * @param participant sequenceParticipant
	 */
	private void checkSequenceFeatures(sequenceParticipant participant) {

		// get list
		Set<sequenceFeature> sequenceFeatureList = participant.getSEQUENCE_FEATURE_LIST();
		Assert.assertEquals(1, sequenceFeatureList.size());

		// get feature
		boolean featureFound = false;
		sequenceFeature bpSequenceFeature = null;
		for (sequenceFeature sf : sequenceFeatureList) {
			if (sf.getRDFId().equals("HTTP://PATHWAYCOMMONS.ORG/PSI2BP#_2681741952554656410")) {
				featureFound = true;
				bpSequenceFeature = sf;
				break;
			}
		}
		Assert.assertTrue(featureFound);
		
        // get feature type
		boolean featureTypeFound = false;
        openControlledVocabulary featureType = bpSequenceFeature.getFEATURE_TYPE();
		Set<String> featureTypes = featureType.getTERM();
		for (String ft : featureTypes) {
			if (ft.equals("ha tagged")) {
				featureTypeFound = true;
				break;
			}
		}
		Assert.assertTrue(featureTypeFound);

        // feature location
        Set<sequenceLocation> featureLocation = bpSequenceFeature.getFEATURE_LOCATION();
        Assert.assertEquals(0, featureLocation.size());
	}

	/**
	 * Checks Physical Entity attributesgiven sequence participant.
	 *
	 * @param participant sequenceParticipant
	 */
	private void checkPhysicalEntity(sequenceParticipant participant) {

		// get physical entity
		physicalEntity bpPhysicalEntity = participant.getPHYSICAL_ENTITY();
		Assert.assertEquals("HTTP://PATHWAYCOMMONS.ORG/PSI2BP#_8", bpPhysicalEntity.getRDFId());
		Assert.assertEquals("DNA primase small subunit", bpPhysicalEntity.getNAME());
		Assert.assertEquals("pri1_mouse", bpPhysicalEntity.getSHORT_NAME());

		// get physical entity xref list
		Set<xref> physicalEntityXRefList = bpPhysicalEntity.getXREF();
		Assert.assertEquals(4, physicalEntityXRefList.size());

		// get physical entity unification xref
		xref physicalEntityXRef = null;
		for(xref x : physicalEntityXRefList) {
			if(x instanceof unificationXref) {
				physicalEntityXRef = x;
				break;
			}
		}
		if(physicalEntityXRef == null) {
			fail("no unification xrefs found!");
		}
		
		Assert.assertEquals("HTTP://PATHWAYCOMMONS.ORG/PSI2BP#UXR-P20664", physicalEntityXRef.getRDFId());
		Assert.assertEquals("uniprotkb", physicalEntityXRef.getDB());
		Assert.assertEquals("P20664", physicalEntityXRef.getID());

		// organism
		bioSource bpBioSource = ((sequenceEntity)bpPhysicalEntity).getORGANISM();
		Assert.assertEquals("Mus musculus", bpBioSource.getNAME());
		Assert.assertEquals("HTTP://PATHWAYCOMMONS.ORG/PSI2BP#BS-10090", bpBioSource.getRDFId());
		unificationXref bioSourceXRef = bpBioSource.getTAXON_XREF();
		Assert.assertEquals("HTTP://PATHWAYCOMMONS.ORG/PSI2BP#_8992476572203004810", bioSourceXRef.getRDFId());
		Assert.assertEquals("TAXONOMY", bioSourceXRef.getDB());
		Assert.assertEquals("10090", bioSourceXRef.getID());

		// sequence
		Assert.assertEquals("MEPFDPAELPELLKLYYRRLFPYAQYYRWLNYGGVTKNYFQHREFSFTLKDDIYIRYQSFNNQSELEKEMQKMNPYKIDIGAVYSHRPNQHNTVKLGAFQAQEKELVFDIDMTDYDDVRRCCSSADICSKCWTLMTMAMRIIDRALKEDFGFKHRLWVYSGRRGVHCWVCDESVRKLSSAVRSGIVEYLSLVKGGQDVKKKVHLNEKVHPFVRKSINIIKKYFEEYALVGQDILENKENWDKILALVPETIHDELQRGFQKFHSSPQRWEHLRKVANSSQNMKNDKCGPWLEWEVMLQYCFPRLDVNVSKGVNHLLKSPFSVHPKTGRISVPIDFHKVDQFDPFTVPTISAICRELDMVSTHEKEKEENEADSKHRVRGYKKTSLAPYVKVFEQFLENLDKSRKGELLKKSDLQKDF", ((sequenceEntity)bpPhysicalEntity).getSEQUENCE());
	}
}
