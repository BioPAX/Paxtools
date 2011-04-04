package org.biopax.paxtools.io.gsea;

import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * GSEA conversion test.
 * TODO: need to check accuracy of GSEA content
 */
public class GSEAConverterTest {

	PrintStream out = null;
	static BioPAXIOHandler handler =  new SimpleIOHandler();
	static final String outFile = "target" + File.separator + "gseaConverterTest.out.txt";
	
	
	@Before
	public void setupTest() throws IOException {
		out = new PrintStream(new FileOutputStream(outFile, true));
	}
	
	@After
	public void finishTest() throws IOException {
		out.flush();
		out.close();
	}
	
	@Test
	public void testWriteL2GSEA() throws Exception {

		// grab all owl in test resource dir L2
		File testDir = new File(getClass().getResource("/L2").getFile());
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return (name.endsWith("owl"));
			}
		};

		// write the output
		out.println("testWriteL2GSEA:");
		for (String testFile : testDir.list(filter)) {
			InputStream in = getClass().getResourceAsStream("/L2/" + testFile); // this is classpath - no need to use a "separator"
			Model level2 = handler.convertFromOWL(in);
			GSEAConverter gseaConverter = new GSEAConverter("GENE_SYMBOL", true);
			Collection<? extends GSEAEntry> entries = gseaConverter.convert(level2);
			// assert some things
			assertEquals(1, entries.size());
			GSEAEntry entry = entries.iterator().next();
			assertEquals("mTOR signaling pathway", entry.getName());
			assertEquals("Pathway Interaction Database NCI-Nature Curated Data", entry.getDataSource());
			assertEquals("9606", entry.getTaxID());
			Map<String,String> rdfToGenes = entry.getRDFToGeneMap();
			assertEquals(27, rdfToGenes.size());
			// dump the output
			(new GSEAConverter("GENE_SYMBOL", true)).writeToGSEA(level2, out);
			in.close();
		}
	}
    
	@Test
	public void testWriteL3GSEA() throws Exception {

		// grab all owl in test resource dir L3
		File testDir = new File(getClass().getResource("/L3").getFile());
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return (name.endsWith("owl"));
			}
		};

		// write the output
		out.println("testWriteL3GSEA:");
		for (String testFile : testDir.list(filter)) {
			InputStream in = getClass().getResourceAsStream("/L3/" + testFile); // this is classpath - no need to use a "separator"
			Model level3 = handler.convertFromOWL(in);
			GSEAConverter gseaConverter = new GSEAConverter("uniprot", true);
			Collection<? extends GSEAEntry> entries = gseaConverter.convert(level3);
			// assert some things
			assertEquals(1, entries.size());
			GSEAEntry entry = entries.iterator().next();
			assertEquals("Glycolysis Pathway", entry.getName() );
			//FIXME it can return either aMAZE or "KEGG" below; - because the order is not defined in gseaConverter.convert!
			assertTrue("aMAZE".equals(entry.getDataSource()) || "KEGG".equals(entry.getDataSource()));
			assertEquals("562", entry.getTaxID());
			Map<String,String> rdfToGenes = entry.getRDFToGeneMap();
			assertEquals(2, rdfToGenes.size());
			for (String aSymbol : rdfToGenes.values()) {
				assertTrue(aSymbol.equals("P46880") || aSymbol.equals("Q9KH85"));
			}
			// dump the output
			(new GSEAConverter("uniprot", true)).writeToGSEA(level3, out);
			in.close();
		}
	}
}