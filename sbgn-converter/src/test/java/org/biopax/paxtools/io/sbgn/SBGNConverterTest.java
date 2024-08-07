package org.biopax.paxtools.io.sbgn;

import org.apache.commons.lang3.StringUtils;
import org.biopax.paxtools.impl.MockFactory;
import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.util.Blacklist;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class SBGNConverterTest
{
	private static final BioPAXIOHandler handler =  new SimpleIOHandler();
	private static UbiqueDetector blacklist;
	private static Unmarshaller unmarshaller;
	private static Marshaller marshaller;
	private final String NOSPACE = "\\S+";

	@BeforeAll
	public static void setUp() throws JAXBException {
		blacklist = new ListUbiqueDetector(new HashSet<>(Arrays.asList(
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
	public void toSBGNConversion() throws Exception
	{
		String input = "/AR-TP53";
		InputStream in = getClass().getResourceAsStream(input + ".owl");
		Model level3 = handler.convertFromOWL(in);
		String out = "target" + input + ".sbgn";
		L3ToSBGNPDConverter conv = new L3ToSBGNPDConverter(blacklist, null, true);
		conv.writeSBGN(level3, out);
		File outFile = new File(out);
		SbgnUtil.isValid(outFile); //ignore CName warning

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
				assertTrue(g.getId().matches(NOSPACE)); //refs issue #44
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
	public void nestedComplexes() throws Exception
	{
		String input = "/translation-initiation-complex-formation";
		InputStream in = getClass().getResourceAsStream(input + ".owl");
		Model level3 = handler.convertFromOWL(in);

		String out = "target" + input + ".sbgn";
		L3ToSBGNPDConverter conv = new L3ToSBGNPDConverter(null, null, true);
		conv.setFlattenComplexContent(false);
		conv.writeSBGN(level3, out);
		File outFile = new File(out);
		SbgnUtil.isValid(outFile); //ignore CName warning

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
	public void stoichiometry() throws Exception
	{
		String input = "/Stoic";
		InputStream in = getClass().getResourceAsStream(input + ".owl");
		Model level3 = handler.convertFromOWL(in);

		String out = "target" + input + ".sbgn";

		L3ToSBGNPDConverter conv = new L3ToSBGNPDConverter();
		conv.setDoLayout(true);
		conv.writeSBGN(level3, out);

		File outFile = new File(out);
		SbgnUtil.isValid(outFile); //ignore CName warning
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
	public void convertBmpPathway() throws Exception
	{
		String input = "/signaling-by-bmp";
		InputStream in = getClass().getResourceAsStream(input + ".owl");
		Model level3 = handler.convertFromOWL(in);
		String out = "target" + input + ".sbgn";

		L3ToSBGNPDConverter conv = new L3ToSBGNPDConverter(blacklist,null, false);
		conv.setDoLayout(true); //this is not the default anymore
		conv.writeSBGN(level3, out);

		File outFile = new File(out);
		SbgnUtil.isValid(outFile); //ignore CName warning
		// Now read the SBGN model back
		Sbgn result = (Sbgn) unmarshaller.unmarshal (outFile);

		// Assert that the sbgn result contains glyphs
		assertTrue(!result.getMap().getGlyph().isEmpty());

		// Assert that compartments do not contain members inside
		for (Glyph g : result.getMap().getGlyph()) {
			if (g.getClazz().equals("compartment")) {
				assertTrue(g.getGlyph().isEmpty());
				assertTrue(g.getId().matches(NOSPACE));
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
	public void convertOmittedSmpdbPathway() throws Exception {
		String input = "/smpdb-beta-oxidation";
		InputStream in = getClass().getResourceAsStream(input + ".owl");
		Model level3 = handler.convertFromOWL(in);
		String out = "target" + input + ".sbgn";
		L3ToSBGNPDConverter conv = new L3ToSBGNPDConverter(blacklist,null, false);
		conv.writeSBGN(level3, out);
		SbgnUtil.isValid(new File(out)); //ignore CName warning
		Sbgn result = (Sbgn) unmarshaller.unmarshal(new File(out));
		assertFalse(result.getMap().getGlyph().isEmpty());
    //TODO: add assertions
	}

	@Test
	public void convertBadWikiPathway() throws Exception {
		String input = "/WP561";
		InputStream in = getClass().getResourceAsStream(input + ".owl");
		Model level3 = handler.convertFromOWL(in);
		String out = "target" + input + ".sbgn";

		L3ToSBGNPDConverter conv = new L3ToSBGNPDConverter(blacklist,null, true);
		conv.writeSBGN(level3, out);
		File outFile = new File(out);
		SbgnUtil.isValid(outFile); //ignore validation errors for now...
		Sbgn result = (Sbgn) unmarshaller.unmarshal(outFile);
		assertFalse(result.getMap().getGlyph().isEmpty());
		//TODO: add assertions
	}

	@Test
	public void sbgnLayoutKegg51() throws Exception
	{
		File sbgnFile = new File(getClass().getResource("/hsa00051.sbgn").getFile());

		// Now read from "f" and put the result in "sbgn"
		Sbgn result = (Sbgn)unmarshaller.unmarshal (sbgnFile);
		// Assert that the sbgn result contains glyphs
		assertFalse(result.getMap().getGlyph().isEmpty());

		// infinite loop in LGraph.updateConnected when SbgnPDLayout is used
		(new SBGNLayoutManager()).createLayout(result, true);
		//TODO: run, add assertions
		marshaller.marshal(result, new FileOutputStream("target/hsa00051.out.sbgn"));
	}

	@Test
	public void convertGIs() throws JAXBException {
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

		Sbgn result = (Sbgn) unmarshaller.unmarshal(new File(out));
		assertFalse(result.getMap().getGlyph().isEmpty());
	}

	@Test
	public void convertTRs() throws JAXBException {
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

		// no template - will infer it from the other participant
		// (this is a bad practice though - 'template' prop must be used instead)
		TemplateReaction tr1 = m.addNew(TemplateReaction.class,"tr_1");
		f.bindInPairs("product", tr1,p[2]);
		f.bindInPairs("participant", tr1,t[1]); //this call might be ignored or throw an ex. in the future

		// only product (will infer some unknown input process/entity)
		TemplateReaction tr2 = m.addNew(TemplateReaction.class,"tr_2");
		f.bindInPairs("product", tr2,p[3]);

		L3ToSBGNPDConverter conv = new L3ToSBGNPDConverter();
		conv.setDoLayout(true);
		conv.writeSBGN(m, out);

		Sbgn result = (Sbgn) unmarshaller.unmarshal(new File(out));
		assertFalse(result.getMap().getGlyph().isEmpty());
	}

	@Test
	public void convertTfap2Pathway() throws Exception
	{
		String input = "/TFAP2";
		InputStream in = getClass().getResourceAsStream(input + ".owl");
		Model level3 = handler.convertFromOWL(in);
		String out = "target" + input + ".sbgn";

		L3ToSBGNPDConverter conv = new L3ToSBGNPDConverter(blacklist,null, false);
		conv.setDoLayout(true); //this is not the default anymore
		conv.writeSBGN(level3, out);

		File outFile = new File(out);
		SbgnUtil.isValid(outFile); //ignore CName warning

		// Now read the SBGN model back
		Sbgn result = (Sbgn) unmarshaller.unmarshal (outFile);
		// Assert that the sbgn result contains glyphs
    List<Glyph> glyphList = result.getMap().getGlyph();
		assertFalse(glyphList.isEmpty());
	}

	@Test
	public void convertActivation() throws Exception {
		String input = "/activation";
		InputStream in = getClass().getResourceAsStream(input + ".owl");
		Model m = handler.convertFromOWL(in);
		m.setName("activation");
		String out = "target" + input + ".sbgn";
		L3ToSBGNPDConverter conv = new L3ToSBGNPDConverter(blacklist,null, true);
		conv.writeSBGN(m, out);
		File outFile = new File(out);
		SbgnUtil.isValid(outFile); //ignore CName warning
    Sbgn result = (Sbgn) unmarshaller.unmarshal(outFile);
    assertFalse(result.getMap().getGlyph().isEmpty());
    Collection<Arc> filtered = filterArcsByClazz(result.getMap().getArc(), "stimulation");
    assertFalse(filtered.isEmpty());
	}

	@Test
	public void convertModulation() throws Exception {
		String input = "/modulation";
		InputStream in = getClass().getResourceAsStream(input + ".owl");
		Model level3 = handler.convertFromOWL(in);
		String out = "target" + input + ".sbgn";
		L3ToSBGNPDConverter conv = new L3ToSBGNPDConverter(blacklist,null, true);
		conv.writeSBGN(level3, out);
		File outFile = new File(out);
		SbgnUtil.isValid(outFile); //ignore CName warning
    Sbgn result = (Sbgn) unmarshaller.unmarshal(outFile);
    assertFalse(result.getMap().getGlyph().isEmpty());
    Collection<Arc> filtered = filterArcsByClazz(result.getMap().getArc(), "stimulation");
    assertFalse(filtered.isEmpty());
	}

  @Test
  public void convertControlsChain() throws Exception {
    String input = "/controlchain";
    InputStream in = getClass().getResourceAsStream(input + ".owl");
    Model level3 = handler.convertFromOWL(in);
    String out = "target" + input + ".sbgn";
    L3ToSBGNPDConverter conv = new L3ToSBGNPDConverter(blacklist,null, true);
    conv.writeSBGN(level3, out);
		File outFile = new File(out);
		SbgnUtil.isValid(outFile); //ignore CName warning
    Sbgn result = (Sbgn) unmarshaller.unmarshal(outFile);
    assertFalse(result.getMap().getGlyph().isEmpty());
    Collection<Arc> filtered = filterArcsByClazz(result.getMap().getArc(), "stimulation");
    assertFalse(filtered.isEmpty());
  }

	private Collection<Glyph> filterGlyphsByClazz(Collection<Glyph> collection, String clazz) {
    return collection.stream().filter(g -> g.getClazz().equals(clazz)).collect(Collectors.toUnmodifiableSet());
  }

  private Collection<Arc> filterArcsByClazz(Collection<Arc> collection, String clazz) {
		return collection.stream().filter(g -> g.getClazz().equals(clazz)).collect(Collectors.toUnmodifiableSet());
  }

	@Disabled
	@Test
	public void convertPc14NearestNeighborhoodOfANK1_PTEN() throws Exception
	{
		Model level3 = handler.convertFromOWL(Files.newInputStream(Paths.get("/home/igor/Downloads/cpath2-issue-321-pc14-nhood-ank1-pten-data.owl")));
		String out = "target/cpath2-321.sbgn";

		UbiqueDetector bl = new ListUbiqueDetector(new Blacklist("/home/igor/Workspace/pc-stack/work/downloads/blacklist.txt").getListed());
		L3ToSBGNPDConverter conv = new L3ToSBGNPDConverter(bl,null, false);
		conv.writeSBGN(level3, out);

		File outFile = new File(out);
		SbgnUtil.isValid(outFile); //ignore CName warning
		// Now read the SBGN model back
		Sbgn result = (Sbgn) unmarshaller.unmarshal (outFile);
		// Assert that the sbgn result contains glyphs
		List<Glyph> glyphList = result.getMap().getGlyph();
		assertFalse(glyphList.isEmpty());
	}
}
