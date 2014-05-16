package org.biopax.paxtools.io.sbgn;

import static junit.framework.Assert.*;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.junit.Ignore;
import org.junit.Test;
import org.sbgn.GlyphClazz;
import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Sbgn;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class SBGNConverterTest
{
	static BioPAXIOHandler handler =  new SimpleIOHandler();

	@Test
	public void testSBGNConversion() throws JAXBException, IOException, SAXException
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
	public void testNestedComplexes() throws JAXBException, IOException, SAXException
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

	@Ignore
	@Test
	public void testStoichiometry() throws JAXBException, IOException, SAXException
	{
		String input = "/Stoic";
		InputStream in = getClass().getResourceAsStream(input + ".owl");
		Model level3 = handler.convertFromOWL(in);

		String out = "target/" + input + ".sbgn";

		L3ToSBGNPDConverter conv = new L3ToSBGNPDConverter(null, null, true);

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
}
