package org.biopax.paxtools.io.gsea;

import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Collection;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 * GSEA (GMT) conversion test.
 */
public class GSEAConverterTest {

	public GSEAConverterTest() {
	}

	static BioPAXIOHandler handler =  new SimpleIOHandler();

	@Test
	public final void writeL2GSEA() throws Exception {
		// write the output
		InputStream in = getClass().getResourceAsStream("/L2/biopax_id_557861_mTor_signaling.owl");
		Model level2 = handler.convertFromOWL(in);
		// GENE_SYMBOL should be resolved as hgnc.symbol prefix (bioregistry)
		GSEAConverter gseaConverter = new GSEAConverter("GENE_SYMBOL", true);
		Collection<GMTEntry> entries = gseaConverter.convert(level2);
		Assertions.assertEquals(1, entries.size());
		GMTEntry entry = entries.iterator().next();

		Assertions.assertEquals("http://cbio.mskcc.org/cpathCPATH-557861", entry.name());
		Assertions.assertTrue(entry.description().contains("mTOR signaling pathway"));
		Assertions.assertTrue(entry.description().contains("nci-nature"));
		Assertions.assertEquals("9606", entry.taxID());
		Assertions.assertEquals("hgnc.symbol", entry.idType());
		Assertions.assertEquals(27, entry.identifiers().size());

//		// dump the output
//		(new GSEAConverter("hgnc.symbol", true)).writeToGSEA(level2, System.out);

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
		Assertions.assertEquals("refseq", entry.idType());
		Assertions.assertEquals(33, entry.identifiers().size());
	}
    
	@Test
	public final void writeL3GSEA() throws Exception {
		InputStream in = getClass().getResourceAsStream("/L3/biopax3-short-metabolic-pathway.owl");
		Model level3 = handler.convertFromOWL(in);
		GSEAConverter gseaConverter = new GSEAConverter("uniprot", true);
		Collection<? extends GMTEntry> entries = gseaConverter.convert(level3);
		Assertions.assertFalse(entries.isEmpty());
	}

	@Test
	public final void writePc14TestToGSEA() throws Exception {
		InputStream in = new GZIPInputStream(getClass().getResourceAsStream("/pc14test.owl.gz"));
		Model m = handler.convertFromOWL(in);
		GSEAConverter gseaConverter = new GSEAConverter("gene symbol", true);
		gseaConverter.setAllowedOrganisms(Set.of("9606"));
		Collection<? extends GMTEntry> entries = gseaConverter.convert(m);
		Assertions.assertEquals("hgnc.symbol", gseaConverter.getIdType()); //"gene symbol" is resolved to hgnc.symbol
		Assertions.assertFalse(entries.isEmpty());
//		gseaConverter.writeToGSEA(m, System.out);
	}
}