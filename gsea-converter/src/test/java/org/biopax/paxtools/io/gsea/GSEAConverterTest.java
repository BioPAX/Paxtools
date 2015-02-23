package org.biopax.paxtools.io.gsea;

import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * GSEA conversion test.
 * TODO: need to check accuracy of GSEA content
 */
public class GSEAConverterTest {

	static PrintStream out = null;
	static BioPAXIOHandler handler =  new SimpleIOHandler();
	static final String outFile = GSEAConverterTest.class
			.getResource("/").getFile() + File.separator + "gseaConverterTest.out.txt";
	
	
	@BeforeClass
	public static void setupTest() throws IOException {
		out = new PrintStream(new FileOutputStream(outFile, false));
	}
	
	@AfterClass
	public static void endTest() throws IOException {
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
			
			// test a small hack that allows using sometimes non-standard DB names, like NP, GO, if they are part of xref.id ;)
			gseaConverter = new GSEAConverter("NP", true);
			entries.clear();
			entries = gseaConverter.convert(level2);
			// assert some things
			assertEquals(1, entries.size());
			entry = entries.iterator().next();
			assertEquals("mTOR signaling pathway", entry.name());
			assertTrue(entry.description().contains("nci-nature"));
			assertEquals("9606", entry.taxID());
			assertEquals("NP", entry.idType());
			assertEquals(33, entry.getIdentifiers().size());
		
			(new GSEAConverter("NP", true)).writeToGSEA(level2, out);
			
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
			//TODO check anything?
			// dump the output
			(new GSEAConverter("uniprot", true)).writeToGSEA(level3, out);
			in.close();
		}
	}
}