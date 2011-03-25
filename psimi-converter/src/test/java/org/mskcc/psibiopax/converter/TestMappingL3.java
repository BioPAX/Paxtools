// $Id: TestMappingL3.java,v 1.2 2009/11/23 13:59:42 rodche Exp $
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
import org.biopax.paxtools.model.level3.*;
import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.model.Entry;
import psidev.psi.mi.xml.model.EntrySet;

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * This tests that a PSI-MI document (level 2-compact) is correctly mapped into a biopax model (L3).
 *
 * @author Benjamin Gross
 */
public class TestMappingL3 extends TestCase implements BioPAXMarshaller {

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
        return new TestSuite(TestMappingL3.class);
    }

	/**
	 * The big deal main - if we want to run from command line.
	 */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

	/**
     * Tests that a PSI document (level 2) is correctly mapped into a biopax model.
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
			BioPAXMapper bpMapper = new BioPAXMapperImp(BioPAXLevel.L3);
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
		Assert.assertEquals(122, biopaxElements.size());

		// get the element
		BioPAXElement bpElement = null;
		for (BioPAXElement biopaxElement : biopaxElements) {
			if (biopaxElement.getRDFId().equals("HTTP://PATHWAYCOMMONS.ORG/PSI2BP#_8694781173405777161")) {
				bpElement = biopaxElement;
				break;
			}
		}
		Assert.assertTrue(bpElement != null);

		// cast to interaction
		MolecularInteraction bpInteraction = (MolecularInteraction)bpElement;

		// participants
		Set<? extends Entity> interactionParticipants = bpInteraction.getParticipant();
		Assert.assertEquals(2, interactionParticipants.size());

		// get participant
		Protein participant = null;
		for (PhysicalEntity interactionParticipant : (Set<PhysicalEntity>)interactionParticipants) {
			if (interactionParticipant.getRDFId().equals("HTTP://PATHWAYCOMMONS.ORG/PSI2BP#_8")) {
				participant = (Protein)interactionParticipant;
			}
		}
		Assert.assertTrue(participant != null);

		// check sequence feature list/sequence feature
		checkSequenceFeatures(participant);
		
		// physical entity
		checkPhysicalEntity(participant);

	}

	/**
	 * Checks sequence feature list of given protein.
	 *
	 * @param participant Protein
	 */
	private void checkSequenceFeatures(Protein participant) {

		// get list
		Set<EntityFeature> entityFeatureList = participant.getFeature();
		Assert.assertEquals(4, entityFeatureList.size());

		// get feature
		boolean featureFound = false;
		EntityFeature bpEntityFeature = null;
		for (EntityFeature ef : entityFeatureList) {
			if (ef.getRDFId().equals("HTTP://PATHWAYCOMMONS.ORG/PSI2BP#_2681741952554656410")) {
				featureFound = true;
				bpEntityFeature = ef;
				break;
			}
		}

        // feature location
        SequenceLocation sequenceLocation = bpEntityFeature.getFeatureLocation();
        Assert.assertEquals(null, sequenceLocation);

        // get feature location type
		boolean featureLocationTypeFound = false;
        SequenceRegionVocabulary featureLocationType = bpEntityFeature.getFeatureLocationType();
		Set<String> featureLocationTypes = featureLocationType.getTerm();
		for (String flt : featureLocationTypes) {
			if (flt.equals("ha tagged")) {
				featureLocationTypeFound = true;
				break;
			}
		}
		Assert.assertTrue(featureLocationTypeFound);
	}

	/**
	 * Checks Physical Entity attributesgiven sequence participant.
	 *
	 * @param participant sequenceParticipant
	 */
	private void checkPhysicalEntity(Protein participant) {

		// get physical entity
		Assert.assertEquals("HTTP://PATHWAYCOMMONS.ORG/PSI2BP#_8", participant.getRDFId());
		Assert.assertEquals("Prim1", participant.getName().iterator().next());
		
		// get physical entity xref list
		Set<Xref> physicalEntityXRefList = participant.getXref();
		Assert.assertEquals(4, physicalEntityXRefList.size());

		// get physical entity xref
		Xref physicalEntityXRef = null;
		for(Xref x : physicalEntityXRefList) {
			if(x instanceof UnificationXref) {
				physicalEntityXRef = x;
				break;
			}
		}
		if(physicalEntityXRef == null) {
			fail("no unification xrefs found!");
		}
		
		Assert.assertEquals("HTTP://PATHWAYCOMMONS.ORG/PSI2BP#UXR-P20664", physicalEntityXRef.getRDFId());
		Assert.assertEquals("uniprotkb", physicalEntityXRef.getDb());
		Assert.assertEquals("P20664", physicalEntityXRef.getId());

		// get protein reference
		ProteinReference proteinReference = (ProteinReference)participant.getEntityReference();

		// organism
		BioSource bpBioSource = proteinReference.getOrganism();
		Assert.assertEquals("Mus musculus", bpBioSource.getName().iterator().next());
		Assert.assertEquals("HTTP://PATHWAYCOMMONS.ORG/PSI2BP#BS-10090", bpBioSource.getRDFId());
		Set<Xref> bioSourceXRef = bpBioSource.getXref();
		Assert.assertEquals(1, bioSourceXRef.size());
		for (Xref unificationXref : bioSourceXRef) {
			Assert.assertEquals("HTTP://PATHWAYCOMMONS.ORG/PSI2BP#_8992476572203004810", unificationXref.getRDFId());
			Assert.assertEquals("TAXONOMY", unificationXref.getDb());
			Assert.assertEquals("10090", unificationXref.getId());
		}

		// sequence
		Assert.assertEquals("MEPFDPAELPELLKLYYRRLFPYAQYYRWLNYGGVTKNYFQHREFSFTLKDDIYIRYQSFNNQSELEKEMQKMNPYKIDIGAVYSHRPNQHNTVKLGAFQAQEKELVFDIDMTDYDDVRRCCSSADICSKCWTLMTMAMRIIDRALKEDFGFKHRLWVYSGRRGVHCWVCDESVRKLSSAVRSGIVEYLSLVKGGQDVKKKVHLNEKVHPFVRKSINIIKKYFEEYALVGQDILENKENWDKILALVPETIHDELQRGFQKFHSSPQRWEHLRKVANSSQNMKNDKCGPWLEWEVMLQYCFPRLDVNVSKGVNHLLKSPFSVHPKTGRISVPIDFHKVDQFDPFTVPTISAICRELDMVSTHEKEKEENEADSKHRVRGYKKTSLAPYVKVFEQFLENLDKSRKGELLKKSDLQKDF", proteinReference.getSequence());
	}
}
