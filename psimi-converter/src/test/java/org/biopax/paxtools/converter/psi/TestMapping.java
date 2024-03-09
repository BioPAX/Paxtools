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

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.converter.tab2xml.Tab2Xml;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.xml.PsimiXmlReader;
import psidev.psi.mi.xml.model.Entry;
import psidev.psi.mi.xml.model.EntrySet;
import psidev.psi.mi.xml.model.Interaction;
import psidev.psi.mi.xml.model.Participant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.zip.GZIPInputStream;

/**
 *
 * This test depends upon a particular random number generator
 * with a particular seed.
 *
 *
 * @author rodche (Igor Rodchenkov)
 */
public class TestMapping  {

	/**
	 * psi-mi test file
	 */
	private static final String PSI_MI_TEST_FILE = "10523676-compact.xml.gz";

	/**
	 * psi-mitab test file
	 */
	private static final String PSI_MITAB_TEST_FILE = "12167173.txt";


	/**
	 * A test data excerpt from BIND PSI-MI XML
	 */
	private static final String BIND_TEST_FILE = "bind-test.psimi.xml";

	private static final String INTACT_TEST_FILE = "human_31.xml.gz";

	@Test
	public void testApi() throws Exception {
		// unmarshall the data, close the stream
		PsimiXmlReader reader = new PsimiXmlReader();
		InputStream is = new GZIPInputStream(getClass().getClassLoader().getResourceAsStream(PSI_MI_TEST_FILE));
		EntrySet es = reader.read(is);
		is.close();
		Collection<Entry> entries =  es.getEntries();
		// we should only have 1 entry
		assertEquals(1, entries.size());
		// get entry
		Entry entry = entries.iterator().next();

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
	public void mapping() {

		Model bpModel = BioPAXLevel.L3.getDefaultFactory().createModel();

		// open file
		try {
			// unmarshall the data, close the stream
			PsimiXmlReader reader = new PsimiXmlReader();
			InputStream is = new GZIPInputStream(getClass().getClassLoader().getResourceAsStream(PSI_MI_TEST_FILE));
			EntrySet es = reader.read(is);
			is.close();
			Collection<Entry> entries =  es.getEntries();

			// we should only have 1 entry
			assertEquals(1, entries.size());

			// get entry
			Entry entry = entries.iterator().next();
			EntryMapper mapper = new EntryMapper(bpModel, false);
			mapper.run(entry);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		assertNotNull(bpModel);
		assertFalse(bpModel.getObjects().isEmpty());

		//EFs are generated 
		assertFalse(bpModel.getObjects(ExperimentalForm.class).isEmpty());

		//TODO add more assertions
	}


	@Test
	public void mitabToMi() throws Exception {
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

	@Test
	public void participantsAreNotDuplicated() throws IOException {

		Model bpModel = BioPAXLevel.L3.getDefaultFactory().createModel();
		// open file
		try {
			// unmarshall the data, close the stream
			PsimiXmlReader reader = new PsimiXmlReader();
			InputStream is = getClass().getClassLoader().getResourceAsStream(BIND_TEST_FILE);
			EntrySet es = reader.read(is);
			is.close();
			Collection<Entry> entries =  es.getEntries();
			assertEquals(1, entries.size());
			Entry entry = entries.iterator().next();
			EntryMapper mapper = new EntryMapper(bpModel, false);
			mapper.run(entry);
		}
		catch (Exception e) {
			e.printStackTrace();
		}

		assertNotNull(bpModel);
		assertFalse(bpModel.getObjects().isEmpty());

		assertEquals(3, bpModel.getObjects(MolecularInteraction.class).size());

		save(bpModel, getClass().getClassLoader().getResource("").getPath()
				+ File.separator + "testConvertBindPsimi.owl");

		// there are 6 PSI-MI participants that refer to: 6 interactors and 3 experimental interactors 
		// (but these refer to only 5 unique primary xref IDs - 4 of protein and 1 of dna type) 
		// 4 "Max"/"GST-Max" and "Myc-associated factor X" interactors that have same ID makes only one PR
		// 2 HMBS promoter dnas make one DnaReference
		assertEquals(4, bpModel.getObjects(ProteinReference.class).size());
		// 2 dna type interactors and experimental inter. merge into one
		assertEquals(1, bpModel.getObjects(DnaReference.class).size());
		// 9 original physical entities, some were merged (e.g., two gst-max...)
		assertEquals(8, bpModel.getObjects(SimplePhysicalEntity.class).size());

		ProteinReference pr = (ProteinReference) bpModel.getByID("ProteinReference_refseq_NP_002373_identity");
		assertNotNull(pr);
		assertEquals(2, pr.getName().size());
		assertTrue(pr.getName().contains("Max"));
		assertTrue(pr.getName().contains("Myc-associated factor X"));
	}

	@Test
	public void mappingIntAct() {
		Model bpModel = BioPAXLevel.L3.getDefaultFactory().createModel();
		try {
			PsimiXmlReader reader = new PsimiXmlReader();
			InputStream is = new GZIPInputStream(getClass().getClassLoader().getResourceAsStream(INTACT_TEST_FILE));
			EntrySet es = reader.read(is);
			is.close();
			Collection<Entry> entries =  es.getEntries();
			// we should only have 1 entry
			assertEquals(11, entries.size());
			Entry entry = (Entry)entries.iterator().next();
			EntryMapper mapper = new EntryMapper(bpModel, false);
			mapper.run(entry);
			save(bpModel,"target/" + INTACT_TEST_FILE + ".owl");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		assertNotNull(bpModel);
		assertFalse(bpModel.getObjects().isEmpty());
		Collection<Evidence> evs = bpModel.getObjects(Evidence.class);
		assertFalse(evs.isEmpty());
		MolecularInteraction interaction = null;
		for(MolecularInteraction mi : bpModel.getObjects(MolecularInteraction.class)) {
			if(mi.getUri().contains("1580340")) {
				interaction = mi;
				break;
			}
		}
		assertNotNull(interaction);
		assertTrue(interaction.getComment().contains(EntryMapper.FIGURE_LEGEND_CODE+ ":1C"));
	}

	private void save(Model model, String file) throws IOException {
		new SimpleIOHandler(BioPAXLevel.L3).convertToOWL(model, new FileOutputStream(file));
	}
}
