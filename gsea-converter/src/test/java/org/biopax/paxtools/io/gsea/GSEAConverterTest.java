package org.biopax.paxtools.io.gsea;

import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Collection;

/**
 * GSEA (GMT) conversion test.
 */
public class GSEAConverterTest {

	static BioPAXIOHandler handler =  new SimpleIOHandler();

	@Test
	public void writeL2GSEA() throws Exception {
		// write the output
		InputStream in = getClass().getResourceAsStream("/L2/biopax_id_557861_mTor_signaling.owl");
		Model level2 = handler.convertFromOWL(in);
		GSEAConverter gseaConverter = new GSEAConverter("GENE_SYMBOL", true);
		Collection<GMTEntry> entries = gseaConverter.convert(level2);
		// assert some things
		//for(GMTEntry gseaEntry : entries) System.out.println("gsea: " + gseaEntry.toString());
		Assertions.assertEquals(1, entries.size());
		GMTEntry entry = entries.iterator().next();
		//CPATH-557861
		Assertions.assertEquals("http://cbio.mskcc.org/cpathCPATH-557861", entry.name());
		Assertions.assertTrue(entry.description().contains("mTOR signaling pathway"));
		Assertions.assertTrue(entry.description().contains("nci-nature"));
		Assertions.assertEquals("9606", entry.taxID());
		Assertions.assertEquals("GENE_SYMBOL", entry.idType());
		Assertions.assertEquals(27, entry.identifiers().size());
		// dump the output
		(new GSEAConverter("GENE_SYMBOL", true)).writeToGSEA(level2, System.out);

		// NO more hacks that enabled using xref.id prefixes, like 'NP', 'GO', instead of true 'DB' names...
		gseaConverter = new GSEAConverter("NP", true);
		entries.clear();
		entries = gseaConverter.convert(level2);
		Assertions.assertEquals(1, entries.size());
		entry = entries.iterator().next();
		Assertions.assertEquals("http://cbio.mskcc.org/cpathCPATH-557861", entry.name());
		Assertions.assertTrue(entry.identifiers().isEmpty());

		gseaConverter = new GSEAConverter("ref_seq", true); //test data has this non-standard name (instead RefSeq)
		entries.clear();
		entries = gseaConverter.convert(level2);
		// assert some things
		Assertions.assertEquals(1, entries.size());
		entry = entries.iterator().next();
		Assertions.assertEquals("http://cbio.mskcc.org/cpathCPATH-557861", entry.name());
		Assertions.assertTrue(entry.description().contains("nci-nature"));
		Assertions.assertEquals("9606", entry.taxID());
		Assertions.assertEquals("ref_seq", entry.idType());
		Assertions.assertEquals(33, entry.identifiers().size());
		(new GSEAConverter("NP", true)).writeToGSEA(level2, System.out);
	}
    
	@Test
	public void writeL3GSEA() throws Exception {
		InputStream in = getClass().getResourceAsStream("/L3/biopax3-short-metabolic-pathway.owl");
		Model level3 = handler.convertFromOWL(in);
		GSEAConverter gseaConverter = new GSEAConverter("uniprot", true);
		Collection<? extends GMTEntry> entries = gseaConverter.convert(level3);
		(new GSEAConverter("uniprot", true)).writeToGSEA(level3, System.out);
	}
}