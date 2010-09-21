package org.biopax.paxtools.io.gsea;

import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.simpleIO.SimpleReader;

import org.biopax.paxtools.model.Model;

import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.*;

import java.io.*;
import java.util.Map;
import java.util.Collection;

/**
 * GSEA conversion test.
 * TODO: need to check accuracy of GSEA content
 */
public class GSEAConverterTest {

	PrintStream out = null;
	static BioPAXIOHandler handler =  new SimpleReader();
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
			assertEquals(entries.size(), 1);
			GSEAEntry entry = entries.iterator().next();
			assertEquals(entry.getName(), "mTOR signaling pathway");
			assertEquals(entry.getDataSource(), "Pathway Interaction Database NCI-Nature Curated Data");
			assertEquals(entry.getTaxID(), "9606");
			Map<String,String> rdfToGenes = entry.getRDFToGeneMap();
			assertEquals(rdfToGenes.size(), 27);
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
			assertEquals(entries.size(), 1);
			GSEAEntry entry = entries.iterator().next();
			assertEquals(entry.getName(), "Glycolysis Pathway");
			assertEquals(entry.getDataSource(), "aMAZE");
			assertEquals(entry.getTaxID(), "562");
			Map<String,String> rdfToGenes = entry.getRDFToGeneMap();
			assertEquals(rdfToGenes.size(), 2);
			for (String aSymbol : rdfToGenes.values()) {
				assertTrue(aSymbol.equals("P46880") || aSymbol.equals("Q9KH85"));
			}
			// dump the output
			(new GSEAConverter("uniprot", true)).writeToGSEA(level3, out);
			in.close();
		}
	}
}