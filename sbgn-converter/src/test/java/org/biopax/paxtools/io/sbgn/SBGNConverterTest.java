package org.biopax.paxtools.io.sbgn;

import static junit.framework.Assert.*;

import org.apache.commons.lang3.StringUtils;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.junit.Test;
import org.sbgn.GlyphClazz;
import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Sbgn;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.*;

/**
 * @author Ozgun Babur
 */
public class SBGNConverterTest
{
	static final BioPAXIOHandler handler =  new SimpleIOHandler();

	@Test
	public void testSBGNConversion() throws Exception
	{
		String input = "/AR-TP53";
		InputStream in = getClass().getResourceAsStream(input + ".owl");
		Model level3 = handler.convertFromOWL(in);

		Set<String> blacklist = new HashSet<String>(Arrays.asList(
			"http://pid.nci.nih.gov/biopaxpid_685", "http://pid.nci.nih.gov/biopaxpid_678",
			"http://pid.nci.nih.gov/biopaxpid_3119", "http://pid.nci.nih.gov/biopaxpid_3114"));

		System.out.println("level3.getObjects().size() = " + level3.getObjects().size());

		String out = "target/" + input + ".sbgn";

		L3ToSBGNPDConverter conv = new L3ToSBGNPDConverter(
			new ListUbiqueDetector(blacklist), null, true);

		conv.writeSBGN(level3, out);

		File outFile = new File(out);
		boolean isValid = SbgnUtil.isValid(outFile);

		if (isValid)
			System.out.println ("Validation succeeded");
		else
			System.out.println ("Validation failed");

		JAXBContext context = JAXBContext.newInstance("org.sbgn.bindings");
		Unmarshaller unmarshaller = context.createUnmarshaller();

		// Now read from "f" and put the result in "sbgn"
		Sbgn result = (Sbgn)unmarshaller.unmarshal (outFile);

		// Assert that the sbgn result contains glyphs
		assertTrue(!result.getMap().getGlyph().isEmpty());

		// Assert that compartments do not contain members inside
		for (Glyph g : result.getMap().getGlyph())
		{
			if (g.getClazz().equals("compartment"))
			{
				assertTrue(g.getGlyph().isEmpty());
				String label = g.getLabel().getText();
				assertFalse(label.matches("go:"));
				assertTrue(StringUtils.isAllLowerCase(label.substring(0,1)));
			}
		}

		// Assert that the id mapping is not empty.
		assertFalse(conv.getSbgn2BPMap().isEmpty());

		for (Set<String> set : conv.getSbgn2BPMap().values())
		{
			assertFalse(set.isEmpty());
		}
	}

	@Test
	public void testNestedComplexes() throws Exception
	{
		String input = "/translation-initiation-complex-formation";
		InputStream in = getClass().getResourceAsStream(input + ".owl");
		Model level3 = handler.convertFromOWL(in);

		System.out.println("level3.getObjects().size() = " + level3.getObjects().size());

		String out = "target/" + input + ".sbgn";

		L3ToSBGNPDConverter conv = new L3ToSBGNPDConverter(null, null, true);
		conv.setFlattenComplexContent(false);

		conv.writeSBGN(level3, out);

		File outFile = new File(out);
		System.out.println("outFile.getPath() = " + outFile.getAbsolutePath());
		boolean isValid = SbgnUtil.isValid(outFile);

		if (isValid)
			System.out.println ("Validation succeeded");
		else
			System.out.println ("Validation failed");

		JAXBContext context = JAXBContext.newInstance("org.sbgn.bindings");
		Unmarshaller unmarshaller = context.createUnmarshaller();

		// Now read from "f" and put the result in "sbgn"
		Sbgn result = (Sbgn)unmarshaller.unmarshal (outFile);

		boolean hasNestedComplex = false;
		for (Glyph g : result.getMap().getGlyph())
		{
			if (g.getClazz().equals("complex"))
			{
				for (Glyph mem : g.getGlyph())
				{
					if (mem.getClazz().equals("complex"))
					{
						hasNestedComplex = true;
						break;
					}
				}
				if (hasNestedComplex) break;
			}
		}

		assertTrue(hasNestedComplex);
	}

	@Test
	public void testStoichiometry() throws Exception
	{
		String input = "/Stoic";
		InputStream in = getClass().getResourceAsStream(input + ".owl");
		Model level3 = handler.convertFromOWL(in);

		String out = "target/" + input + ".sbgn";

		L3ToSBGNPDConverter conv = new L3ToSBGNPDConverter();
		conv.setDoLayout(true);
		conv.writeSBGN(level3, out);

		File outFile = new File(out);
		boolean isValid = SbgnUtil.isValid(outFile);

		if (isValid)
			System.out.println ("success: " + out + " is valid SBGN");
		else
			System.out.println ("warning: " + out + " is invalid SBGN");

		JAXBContext context = JAXBContext.newInstance("org.sbgn.bindings");
		Unmarshaller unmarshaller = context.createUnmarshaller();

		// Now read from "f" and put the result in "sbgn"
		Sbgn result = (Sbgn)unmarshaller.unmarshal (outFile);

		boolean stoicFound = false;
		for (Arc arc : result.getMap().getArc())
		{
			for (Glyph g : arc.getGlyph())
			{
				if (g.getClazz().equals(GlyphClazz.CARDINALITY.getClazz())) stoicFound = true;
			}
		}

		assertTrue(stoicFound);
	}

	@Test
	public void testConvertBmpPathway() throws Exception
	{
		String input = "/signaling-by-bmp";
		InputStream in = getClass().getResourceAsStream(input + ".owl");
		Model level3 = handler.convertFromOWL(in);
		String out = "target/" + input + ".sbgn";

		//use Pathway Commons blacklist.txt
		Scanner scanner = new Scanner(getClass().getResourceAsStream("/blacklist.txt"));
		Set<String> bl = new HashSet<String>();
		while(scanner.hasNextLine()) {
			bl.add(scanner.nextLine().split("\\t")[0]);
		}
		L3ToSBGNPDConverter conv = new L3ToSBGNPDConverter(
				new ListUbiqueDetector(bl),null, false);
		conv.setDoLayout(true); //this is not the default anymore
		conv.writeSBGN(level3, out);

		File outFile = new File(out);
		boolean isValid = SbgnUtil.isValid(outFile);
		if (isValid)
			System.out.println ("success: " + out + " is valid SBGN");
		else
			System.out.println ("warning: " + out + " is invalid SBGN");

		// Now read the SBGN model back
		Sbgn result = (Sbgn) JAXBContext.newInstance("org.sbgn.bindings")
				.createUnmarshaller().unmarshal (outFile);

		// Assert that the sbgn result contains glyphs
		assertTrue(!result.getMap().getGlyph().isEmpty());

		// Assert that compartments do not contain members inside
		for (Glyph g : result.getMap().getGlyph()) {
			if (g.getClazz().equals("compartment")) {
				assertTrue(g.getGlyph().isEmpty());
			}
		}

		// Assert that the id mapping is not empty.
		assertFalse(conv.getSbgn2BPMap().isEmpty());

		for (Set<String> set : conv.getSbgn2BPMap().values()) {
			assertFalse(set.isEmpty());
		}

	}

	@Test
	public void testSbgnLayoutKegg51() throws Exception
	{
		File sbgnFile = new File(getClass().getResource("/hsa00051.sbgn").getFile());

		if (!SbgnUtil.isValid(sbgnFile))
			System.out.println ("invalid input SBGN");

		JAXBContext context = JAXBContext.newInstance("org.sbgn.bindings");
		Unmarshaller unmarshaller = context.createUnmarshaller();

		// Now read from "f" and put the result in "sbgn"
		Sbgn result = (Sbgn)unmarshaller.unmarshal (sbgnFile);
		// Assert that the sbgn result contains glyphs
		assertTrue(!result.getMap().getGlyph().isEmpty());

		// infinite loop in LGraph.updateConnected when SbgnPDLayout is used
		(new SBGNLayoutManager()).createLayout(result, true);
		//TODO: run, add assertions

		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.marshal(result, new FileOutputStream("target/hsa00051.out.sbgn"));
	}

}
