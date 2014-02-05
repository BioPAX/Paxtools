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

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.junit.Test;

import static org.junit.Assert.*;

import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.converter.tab2xml.Tab2Xml;
import psidev.psi.mi.tab.model.BinaryInteraction;
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
public class TestMappingL3 implements BioPAXMarshaller {

	/**
	 * psi-mi test file
	 */
	private static final String PSI_MI_TEST_FILE = "10523676-compact.xml";
	
	/**
	 * psi-mitab test file
	 */
	private static final String PSI_MITAB_TEST_FILE = "12167173.txt";

	/**
	 * Used for synchronization.
	 */
	private Model bpModel;

    /**
     * Returns the description/name of the test.
	 * 
	 * @return String
     */
    public String getName() {
        return "TestMapping: Tests the proper mapping of a PSI-MI XML file (level 3-compact) to an in memory Paxtools Model";
    }


    /**
     * Tests that a PSI document (level 2) is correctly mapped into a biopax model.
	 */
    @Test
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
            assertEquals(entries.size(), 1);

			// create a biopax mapper
			BioPAXMapper bpMapper = new BioPAXMapperImp(BioPAXLevel.L3);
			bpMapper.setNamespace("");
			// get entry
			Entry entry = (Entry)entries.iterator().next();
			EntryMapper mapper = new EntryMapper(bpMapper, this, entry, 1970);
			mapper.run();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		// check Model returned from mapper process
		checkModel();
	}

 
	/**
	 * Checks the model(s) returned from the mapper.
	 */
	private void checkModel() {

		Iterator it;

		// get the element list
		Set<BioPAXElement> biopaxElements = bpModel.getObjects();
		assertEquals(112, biopaxElements.size());

		// get the element
		BioPAXElement bpElement = null;
		for (BioPAXElement biopaxElement : biopaxElements) {
			if (biopaxElement.getRDFId().equals("8694781173405777161")) {
				bpElement = biopaxElement;
				break;
			}
		}
		assertTrue(bpElement != null);

		// cast to interaction
		MolecularInteraction bpInteraction = (MolecularInteraction)bpElement;

		// participants
		Set<? extends Entity> interactionParticipants = bpInteraction.getParticipant();
		assertEquals(2, interactionParticipants.size());

		// get participant
		Protein participant = null;
		for (PhysicalEntity interactionParticipant : (Set<PhysicalEntity>)interactionParticipants) {
			if (interactionParticipant.getRDFId().equals("8")) {
				participant = (Protein)interactionParticipant;
			}
		}
		assertTrue(participant != null);

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
		assertEquals(4, entityFeatureList.size());

		// get feature
		boolean featureFound = false;
		EntityFeature bpEntityFeature = null;
		for (EntityFeature ef : entityFeatureList) {
			if (ef.getRDFId().equals("2681741952554656410")) {
				featureFound = true;
				bpEntityFeature = ef;
				break;
			}
		}

        // feature location
        SequenceLocation sequenceLocation = bpEntityFeature.getFeatureLocation();
        assertEquals(null, sequenceLocation);

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
		assertTrue(featureLocationTypeFound);
	}

	/**
	 * Checks Physical Entity attributesgiven sequence participant.
	 *
	 * @param participant sequenceParticipant
	 */
	private void checkPhysicalEntity(Protein participant) {

		// get physical entity
		assertEquals("8", participant.getRDFId());
		assertEquals("Prim1", participant.getName().iterator().next());
		
		// get physical entity xref list
		Set<Xref> physicalEntityXRefList = participant.getEntityReference().getXref();
		assertEquals(4, physicalEntityXRefList.size());

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
		
		assertEquals("UXR-P20664", physicalEntityXRef.getRDFId());
		assertEquals("uniprotkb", physicalEntityXRef.getDb());
		assertEquals("P20664", physicalEntityXRef.getId());

		// get protein reference
		ProteinReference proteinReference = (ProteinReference)participant.getEntityReference();

		// organism
		BioSource bpBioSource = proteinReference.getOrganism();
		assertEquals("Mus musculus", bpBioSource.getName().iterator().next());
		assertEquals("BS-10090", bpBioSource.getRDFId());
		Set<Xref> bioSourceXRef = bpBioSource.getXref();
		assertEquals(1, bioSourceXRef.size());
		for (Xref unificationXref : bioSourceXRef) {
			assertEquals("8992476572203004810", unificationXref.getRDFId());
			assertEquals("TAXONOMY", unificationXref.getDb());
			assertEquals("10090", unificationXref.getId());
		}

		// sequence
		assertEquals("MEPFDPAELPELLKLYYRRLFPYAQYYRWLNYGGVTKNYFQHREFSFTLKDDIYIRYQSFNNQSELEKEMQKMNPYKIDIGAVYSHRPNQHNTVKLGAFQAQEKELVFDIDMTDYDDVRRCCSSADICSKCWTLMTMAMRIIDRALKEDFGFKHRLWVYSGRRGVHCWVCDESVRKLSSAVRSGIVEYLSLVKGGQDVKKKVHLNEKVHPFVRKSINIIKKYFEEYALVGQDILENKENWDKILALVPETIHDELQRGFQKFHSSPQRWEHLRKVANSSQNMKNDKCGPWLEWEVMLQYCFPRLDVNVSKGVNHLLKSPFSVHPKTGRISVPIDFHKVDQFDPFTVPTISAICRELDMVSTHEKEKEENEADSKHRVRGYKKTSLAPYVKVFEQFLENLDKSRKGELLKKSDLQKDF", proteinReference.getSequence());
	}

	@Override
	public void marshallData() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addModel(Model bpModel) {
		this.bpModel = bpModel;
	}
	
	
	@Test
	public void testMitabToMi() throws Exception {
		PsimiTabReader reader = new PsimiTabReader();
		InputStream is = getClass().getClassLoader().getResourceAsStream(PSI_MITAB_TEST_FILE);
		Collection<BinaryInteraction> its = reader.read(is);
		is.close();

		assertFalse(its.isEmpty());
		assertEquals(11, its.size());

		EntrySet es = (new Tab2Xml()).convert(its);
		Collection<Entry> entries = es.getEntries();
		
		//only one entry - multiple interactions
		assertEquals(1, entries.size());
		assertEquals(11, entries.iterator().next().getInteractions().size());
	}
}
