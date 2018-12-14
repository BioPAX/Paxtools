package org.biopax.paxtools.io.sbgn;

import static junit.framework.Assert.*;

import org.apache.commons.lang3.StringUtils;
import org.biopax.paxtools.impl.MockFactory;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sbgn.GlyphClazz;
import org.sbgn.SbgnUtil;
import org.sbgn.bindings.Arc;
import org.sbgn.bindings.Glyph;
import org.sbgn.bindings.Sbgn;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.util.*;

public class SBGNConverterTest
{
	private static final BioPAXIOHandler handler =  new SimpleIOHandler();
	private static UbiqueDetector blacklist;
	private static Unmarshaller unmarshaller;
	private static Marshaller marshaller;

	@BeforeClass
	public static void setUp() throws JAXBException {
		blacklist = new ListUbiqueDetector(new HashSet<String>(Arrays.asList(
				"http://pid.nci.nih.gov/biopaxpid_685",
				"http://pid.nci.nih.gov/biopaxpid_678",
				"http://pid.nci.nih.gov/biopaxpid_3119",
				"http://pid.nci.nih.gov/biopaxpid_3114",
				"http://pathwaycommons.org/pc2/SmallMolecule_3037a14ebec3a95b8dab68e6ea5c946f",
				"http://pathwaycommons.org/pc2/SmallMolecule_4ca9a2cfb6a8a6b14cee5d7ed5945364"
		)));

		JAXBContext context = JAXBContext.newInstance("org.sbgn.bindings");
		unmarshaller = context.createUnmarshaller();
		marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	}

	@Test
	public void testSBGNConversion() throws Exception
	{
		String input = "/AR-TP53";
		InputStream in = getClass().getResourceAsStream(input + ".owl");
		Model level3 = handler.convertFromOWL(in);
		System.out.println("level3.getObjects().size() = " + level3.getObjects().size());

		String out = "target/" + input + ".sbgn";

		L3ToSBGNPDConverter conv = new L3ToSBGNPDConverter(blacklist, null, true);

		conv.writeSBGN(level3, out);

		File outFile = new File(out);
		boolean isValid = SbgnUtil.isValid(outFile);

		if (isValid)
			System.out.println ("Validation succeeded");
		else
			System.out.println ("Validation failed");

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

		L3ToSBGNPDConverter conv = new L3ToSBGNPDConverter(blacklist,null, false);
		conv.setDoLayout(true); //this is not the default anymore
		conv.writeSBGN(level3, out);

		File outFile = new File(out);
		boolean isValid = SbgnUtil.isValid(outFile);
		if (isValid)
			System.out.println ("success: " + out + " is valid SBGN");
		else
			System.out.println ("warning: " + out + " is invalid SBGN");

		// Now read the SBGN model back
		Sbgn result = (Sbgn) unmarshaller.unmarshal (outFile);

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

		for (Glyph g : result.getMap().getGlyph()) {
			if (g.getClazz().equals("process")) {
				assertNotNull(g.getCompartmentRef());
			}
		}

	}

	// an SMPDB model does not contain processes that can be converted to SBGN (layout used to throw
	// ArrayIndexOutOfBoundsException; perhaps, converting it makes no sense...);
	// it's probably about an unknown/omitted sub-pathway with known in/out chemicals,
	// but it's expressed in BioPAX badly (a Pathway with one Interaction and PathwayStep, and no comments...)
	@Test
	public void testConvertOmittedSmpdbPathway()
	{
		String input = "/smpdb-beta-oxidation";
		InputStream in = getClass().getResourceAsStream(input + ".owl");
		Model level3 = handler.convertFromOWL(in);
		String out = "target/" + input + ".sbgn";
		L3ToSBGNPDConverter conv = new L3ToSBGNPDConverter(blacklist,null, false);
		conv.writeSBGN(level3, out);
    //TODO: add assertions
	}

	@Test
	public void testConvertBadWikiPathway()
	{
		String input = "/WP561";
		InputStream in = getClass().getResourceAsStream(input + ".owl");
		Model level3 = handler.convertFromOWL(in);
		String out = "target/" + input + ".sbgn";

		L3ToSBGNPDConverter conv = new L3ToSBGNPDConverter(blacklist,null, true);
		conv.writeSBGN(level3, out);
		//TODO: add assertions
	}

	@Test
	public void testSbgnLayoutKegg51() throws Exception
	{
		File sbgnFile = new File(getClass().getResource("/hsa00051.sbgn").getFile());

		if (!SbgnUtil.isValid(sbgnFile))
			System.out.println ("invalid input SBGN");

		// Now read from "f" and put the result in "sbgn"
		Sbgn result = (Sbgn)unmarshaller.unmarshal (sbgnFile);
		// Assert that the sbgn result contains glyphs
		assertTrue(!result.getMap().getGlyph().isEmpty());

		// infinite loop in LGraph.updateConnected when SbgnPDLayout is used
		(new SBGNLayoutManager()).createLayout(result, true);
		//TODO: run, add assertions
		marshaller.marshal(result, new FileOutputStream("target/hsa00051.out.sbgn"));
	}


	@Test
	public void testConvertGIs() {
		String out = "target/test_gi.sbgn";
		MockFactory f = new MockFactory(BioPAXLevel.L3);
		Model m = f.createModel();
		Gene[] g = f.create(m, Gene.class, 2);
		g[0].setDisplayName("g0");
		g[1].setDisplayName("g1");
		GeneticInteraction gi = m.addNew(GeneticInteraction.class,"gi_0");
		f.bindInPairs("participant", gi,g[0],gi,g[1]);
		PhenotypeVocabulary v = m.addNew(PhenotypeVocabulary.class,"lethal");
		v.addTerm("lethal");
		gi.setPhenotype(v);

		L3ToSBGNPDConverter conv = new L3ToSBGNPDConverter();
		conv.setDoLayout(true);
		conv.writeSBGN(m, out);
	}

	@Test
	public void testConvertTRs()
	{
		String out = "target/test_tr.sbgn";
		MockFactory f = new MockFactory(BioPAXLevel.L3);
		Model m = f.createModel();
		DnaRegion[] t = f.create(m, DnaRegion.class, 2);
		RnaRegion[] p = f.create(m, RnaRegion.class, 4);

		// test several TR cases where template, product, participant properties
		// might be modelled in bad way

		// good example
		TemplateReaction tr0 = m.addNew(TemplateReaction.class,"tr_0");
		f.bindInPairs("product", tr0,p[0],tr0,p[1]);
		tr0.setTemplate(t[0]);

		// no template (will infer)
		TemplateReaction tr1 = m.addNew(TemplateReaction.class,"tr_1");
		f.bindInPairs("product", tr1,p[2]);
		f.bindInPairs("participant", tr1,t[1]);

		// only product (will infer some unknown input process/entity)
		TemplateReaction tr2 = m.addNew(TemplateReaction.class,"tr_2");
		f.bindInPairs("product", tr2,p[3]);

		L3ToSBGNPDConverter conv = new L3ToSBGNPDConverter();
		conv.setDoLayout(true);
		conv.writeSBGN(m, out);
	}

	@Test
	public void testConvertTfap2Pathway() throws Exception
	{
		String input = "/TFAP2";
		InputStream in = getClass().getResourceAsStream(input + ".owl");
		Model level3 = handler.convertFromOWL(in);
		String out = "target/" + input + ".sbgn";

		L3ToSBGNPDConverter conv = new L3ToSBGNPDConverter(blacklist,null, false);
		conv.setDoLayout(true); //this is not the default anymore
		conv.writeSBGN(level3, out);

		File outFile = new File(out);
		boolean isValid = SbgnUtil.isValid(outFile);
		if (isValid)
			System.out.println ("success: " + out + " is valid SBGN");
		else
			System.out.println ("warning: " + out + " is invalid SBGN");

		// Now read the SBGN model back
		Sbgn result = (Sbgn) unmarshaller.unmarshal (outFile);

		// Assert that the sbgn result contains glyphs
    List<Glyph> glyphList = result.getMap().getGlyph();
		assertTrue(!glyphList.isEmpty());
	}

	@Test
	public void testConvertActivation() throws JAXBException {
		String input = "/activation";
		InputStream in = getClass().getResourceAsStream(input + ".owl");
		Model m = handler.convertFromOWL(in);
		m.setName("activation");
		String out = "target/" + input + ".sbgn";
		L3ToSBGNPDConverter conv = new L3ToSBGNPDConverter(blacklist,null, true);
		conv.writeSBGN(m, out);

    Sbgn result = (Sbgn) unmarshaller.unmarshal(new File(out));

    assertFalse(result.getMap().getGlyph().isEmpty());
    Collection<Arc> filtered = filterArcsByClazz(result.getMap().getArc(), "stimulation");
    assertFalse(filtered.isEmpty());
	}

	@Test
	public void testConvertModulation() throws JAXBException {
		String input = "/modulation";
		InputStream in = getClass().getResourceAsStream(input + ".owl");
		Model level3 = handler.convertFromOWL(in);
		String out = "target/" + input + ".sbgn";
		L3ToSBGNPDConverter conv = new L3ToSBGNPDConverter(blacklist,null, true);
		conv.writeSBGN(level3, out);

    Sbgn result = (Sbgn) unmarshaller.unmarshal(new File(out));
    assertFalse(result.getMap().getGlyph().isEmpty());
    Collection<Arc> filtered = filterArcsByClazz(result.getMap().getArc(), "stimulation");
    assertFalse(filtered.isEmpty());
	}

  @Test
  public void testConvertControlsChain() throws JAXBException {
    String input = "/controlchain";
    InputStream in = getClass().getResourceAsStream(input + ".owl");
    Model level3 = handler.convertFromOWL(in);
    String out = "target/" + input + ".sbgn";
    L3ToSBGNPDConverter conv = new L3ToSBGNPDConverter(blacklist,null, true);
    conv.writeSBGN(level3, out);

    Sbgn result = (Sbgn) unmarshaller.unmarshal(new File(out));
    assertFalse(result.getMap().getGlyph().isEmpty());
    Collection<Arc> filtered = filterArcsByClazz(result.getMap().getArc(), "stimulation");
    assertFalse(filtered.isEmpty());
  }

	private Collection<Glyph> filterGlyphsByClazz(Collection<Glyph> collection, String clazz) {
    Set<Glyph> filtered = new HashSet<Glyph>();
    for(Glyph g : collection)
      if(g.getClazz().equals(clazz))
        filtered.add(g);

    return filtered;
  }

  private Collection<Arc> filterArcsByClazz(Collection<Arc> collection, String clazz) {
    Set<Arc> filtered = new HashSet<Arc>();
    for(Arc g : collection)
      if(g.getClazz().equals(clazz))
        filtered.add(g);

    return filtered;
  }
}
