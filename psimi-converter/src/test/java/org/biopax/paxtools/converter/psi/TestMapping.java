// $Id: TestMapping.java,v 1.2 2009/11/23 13:59:42 rodche Exp $
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
package org.biopax.paxtools.converter.psi;

import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.ExperimentalForm;
import org.junit.Test;

import static org.junit.Assert.*;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.converter.tab2xml.Tab2Xml;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.model.Entry;
import psidev.psi.mi.xml.model.EntrySet;
import psidev.psi.mi.xml.model.Interaction;
import psidev.psi.mi.xml.model.Participant;

import java.io.InputStream;
import java.util.Collection;

/**
 *
 * This test depends upon a particular random number generator
 * with a particular seed.
 * 
 * TODO write new tests
 * 
 * @author rodche (Igor Rodchenkov)
 */
public class TestMapping  {

	/**
	 * psi-mi test file
	 */
	private static final String PSI_MI_TEST_FILE = "10523676-compact.xml";
	
	/**
	 * psi-mitab test file
	 */
	private static final String PSI_MITAB_TEST_FILE = "12167173.txt";

    /**
     * Returns the description/name of the test.
	 * 
	 * @return String
     */
    public String getName() {
        return "TestMapping: Tests the proper mapping of a PSI-MI XML file (level 2.5 compact) to an in memory Paxtools Model";
    }


    @Test
    public void testApi() throws Exception {

    	// unmarshall the data, close the stream
    	PsimiXmlReader reader = new PsimiXmlReader();
    	InputStream is = getClass().getClassLoader().getResourceAsStream(PSI_MI_TEST_FILE);
    	EntrySet es = reader.read(is);
    	is.close();
    	Collection<Entry> entries =  es.getEntries();			
    	// we should only have 1 entry
    	assertEquals(1, entries.size());
    	// get entry
    	Entry entry = (Entry)entries.iterator().next();

    	assertTrue(entry.hasExperiments());
    	assertEquals(4, entry.getExperiments().size());
    	assertEquals(10, entry.getInteractions().size());

    	Interaction interaction = null;
    	for(Interaction it : entry.getInteractions()) {
    		if(it.getId()==1) {
    			interaction = it;
    			break;
    		}
    	}

    	//despite there is <experimentList><experimentRef> instead of <experimentList><experimentDescription> items -
    	assertFalse(interaction.hasExperimentRefs());
    	assertEquals(0, interaction.getExperimentRefs().size());
    	//(- weird, but this is how PSIMI API works!)	
    	//at the same time the following is set correctly:
    	assertTrue(interaction.hasExperiments());
    	assertEquals(1, interaction.getExperiments().size());

    	assertFalse(interaction.getParticipants().isEmpty());
    	Participant participant = interaction.getParticipants().iterator().next();
    	//despite there was interactorRef,
    	assertFalse(participant.hasInteractorRef());
    	assertNull(participant.getInteractorRef());
    	//at the same time, the following is set:
    	assertTrue(participant.hasInteractor());
    	assertNotNull(participant.getInteractor());

    	assertFalse(participant.hasInteractionRef());
    	assertFalse(participant.hasInteraction());			

    	
    	//TODO assert experimentalRole.hasExperiments()==true always (if there're experiments); experimentalRole.hasExperimentRefs()==false
	}    
    
    
    /**
     * Tests that a PSI document (level 2.5) is correctly mapped into a biopax model.
	 */
    @Test
    public void testMapping() {

		Model bpModel = BioPAXLevel.L3.getDefaultFactory().createModel();
    	
		// open file
		try {
			// unmarshall the data, close the stream
			PsimiXmlReader reader = new PsimiXmlReader();
            InputStream is = getClass().getClassLoader().getResourceAsStream(PSI_MI_TEST_FILE);
			EntrySet es = reader.read(is);
			is.close();
			Collection<Entry> entries =  es.getEntries();
			
			// we should only have 1 entry
            assertEquals(1, entries.size());

			// get entry
			Entry entry = (Entry)entries.iterator().next();
			EntryMapper mapper = new EntryMapper(bpModel, false);
			mapper.run(entry);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
			
		assertNotNull(bpModel);
		assertFalse(bpModel.getObjects().isEmpty());
		
		assertFalse(bpModel.getObjects(ExperimentalForm.class).isEmpty());
		
		//TODO add more assertions
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
