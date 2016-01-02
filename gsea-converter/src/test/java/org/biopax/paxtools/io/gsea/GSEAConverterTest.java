package org.biopax.paxtools.io.gsea;

import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * GSEA conversion test.
 * TODO: need to check accuracy of GSEA content
 */
public class GSEAConverterTest {

	static BioPAXIOHandler handler =  new SimpleIOHandler();
	static PrintStream out;

	@BeforeClass
	public static void setUp() throws IOException {
		out = new PrintStream(new FileOutputStream(
			GSEAConverterTest.class.getResource("/").getFile() + File.separator + "GSEAConverterTest.out.txt", false));
	}

	@Test
	public void testWriteL2GSEA() throws Exception {
		// write the output
		out.println("testWriteL2GSEA:");
		InputStream in = getClass().getResourceAsStream("/L2/biopax_id_557861_mTor_signaling.owl");
		Model level2 = handler.convertFromOWL(in);
		GSEAConverter gseaConverter = new GSEAConverter("GENE_SYMBOL", true);
		Collection<GSEAEntry> entries = gseaConverter.convert(level2);
		// assert some things
		assertEquals(1, entries.size());
		GSEAEntry entry = entries.iterator().next();
		assertEquals("mTOR signaling pathway", entry.name());
		assertTrue(entry.description().contains("nci-nature"));
		assertEquals("9606", entry.taxID());
		assertEquals("GENE_SYMBOL", entry.idType());
		assertEquals(27, entry.getIdentifiers().size());
		// dump the output
		(new GSEAConverter("GENE_SYMBOL", true)).writeToGSEA(level2, out);

		// NO more hacks that enabled using xref.id prefixes, like 'NP', 'GO', instead of true 'DB' names...
		gseaConverter = new GSEAConverter("NP", true);
		entries.clear();
		entries = gseaConverter.convert(level2);
		assertEquals(1, entries.size());
		entry = entries.iterator().next();
		assertEquals("mTOR signaling pathway", entry.name());
		assertTrue(entry.getIdentifiers().isEmpty());

		gseaConverter = new GSEAConverter("ref_seq", true); //test data has this non-standard name (instead RefSeq)
		entries.clear();
		entries = gseaConverter.convert(level2);
		// assert some things
		assertEquals(1, entries.size());
		entry = entries.iterator().next();
		assertEquals("mTOR signaling pathway", entry.name());
		assertTrue(entry.description().contains("nci-nature"));
		assertEquals("9606", entry.taxID());
		assertEquals("ref_seq", entry.idType());
		assertEquals(33, entry.getIdentifiers().size());
		(new GSEAConverter("NP", true)).writeToGSEA(level2, out);
	}
    
	@Test
	public void testWriteL3GSEA() throws Exception {
		// write the output
		out.println("testWriteL3GSEA:");
		InputStream in = getClass().getResourceAsStream("/L3/biopax3-short-metabolic-pathway.owl");
		Model level3 = handler.convertFromOWL(in);
		GSEAConverter gseaConverter = new GSEAConverter("uniprot", true);
		Collection<? extends GSEAEntry> entries = gseaConverter.convert(level3);
		//TODO check anything?
		(new GSEAConverter("uniprot", true)).writeToGSEA(level3, out);
	}
}