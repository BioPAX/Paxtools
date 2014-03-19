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

import org.biopax.paxtools.model.Model;
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

/**
 *
 * This test depends upon a particular random number generator
 * with a particular seed.
 * 
 * TODO write new tests
 * 
 * @author rodche (Igor Rodchenkov)
 */
public class TestMapping extends BioPAXMarshaller {

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

			// get entry
			Entry entry = (Entry)entries.iterator().next();
			EntryMapper mapper = new EntryMapper("", this, entry);
			mapper.run();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		
		assertNotNull(bpModel);
		assertFalse(bpModel.getObjects().isEmpty());
		
		//TODO add more assertions
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
