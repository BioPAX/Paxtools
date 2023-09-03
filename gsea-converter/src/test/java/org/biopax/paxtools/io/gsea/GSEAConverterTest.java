package org.biopax.paxtools.io.gsea;

import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.junit.Test;

import java.io.*;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * GSEA (GMT) conversion test.
 */
public class GSEAConverterTest {

	static BioPAXIOHandler handler =  new SimpleIOHandler();

	@Test
	public void testWriteL2GSEA() throws Exception {
		// write the output
		InputStream in = getClass().getResourceAsStream("/L2/biopax_id_557861_mTor_signaling.owl");
		Model level2 = handler.convertFromOWL(in);
		GSEAConverter gseaConverter = new GSEAConverter("GENE_SYMBOL", true);
		Collection<GMTEntry> entries = gseaConverter.convert(level2);
		// assert some things
		//for(GMTEntry gseaEntry : entries) System.out.println("gsea: " + gseaEntry.toString());
		assertEquals(1, entries.size());
		GMTEntry entry = entries.iterator().next();
		//CPATH-557861
		assertEquals("http://cbio.mskcc.org/cpathCPATH-557861", entry.name());
		assertTrue(entry.description().contains("mTOR signaling pathway"));
		assertTrue(entry.description().contains("nci-nature"));
		assertEquals("9606", entry.taxID());
		assertEquals("GENE_SYMBOL", entry.idType());
		assertEquals(27, entry.identifiers().size());
		// dump the output
		(new GSEAConverter("GENE_SYMBOL", true)).writeToGSEA(level2, System.out);

		// NO more hacks that enabled using xref.id prefixes, like 'NP', 'GO', instead of true 'DB' names...
		gseaConverter = new GSEAConverter("NP", true);
		entries.clear();
		entries = gseaConverter.convert(level2);
		assertEquals(1, entries.size());
		entry = entries.iterator().next();
		assertEquals("http://cbio.mskcc.org/cpathCPATH-557861", entry.name());
		assertTrue(entry.identifiers().isEmpty());

		gseaConverter = new GSEAConverter("ref_seq", true); //test data has this non-standard name (instead RefSeq)
		entries.clear();
		entries = gseaConverter.convert(level2);
		// assert some things
		assertEquals(1, entries.size());
		entry = entries.iterator().next();
		assertEquals("http://cbio.mskcc.org/cpathCPATH-557861", entry.name());
		assertTrue(entry.description().contains("nci-nature"));
		assertEquals("9606", entry.taxID());
		assertEquals("ref_seq", entry.idType());
		assertEquals(33, entry.identifiers().size());
		(new GSEAConverter("NP", true)).writeToGSEA(level2, System.out);
	}
    
	@Test
	public void testWriteL3GSEA() throws Exception {
		InputStream in = getClass().getResourceAsStream("/L3/biopax3-short-metabolic-pathway.owl");
		Model level3 = handler.convertFromOWL(in);
		GSEAConverter gseaConverter = new GSEAConverter("uniprot", true);
		Collection<? extends GMTEntry> entries = gseaConverter.convert(level3);
		(new GSEAConverter("uniprot", true)).writeToGSEA(level3, System.out);
	}
}