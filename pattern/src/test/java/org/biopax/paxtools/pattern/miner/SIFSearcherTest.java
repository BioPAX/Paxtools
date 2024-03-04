package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.Pattern;
import org.biopax.paxtools.pattern.PatternBoxTest;
import org.biopax.paxtools.pattern.constraint.IDConstraint;
import org.biopax.paxtools.pattern.util.AdjacencyMatrix;
import org.biopax.paxtools.pattern.util.Blacklist;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ozgun Babur
 */
public class SIFSearcherTest extends PatternBoxTest
{
	final Logger log = LoggerFactory.getLogger(SIFSearcherTest.class);

	@Test
	public void getPathways()
	{
		SIFSearcher searcher = new SIFSearcher(SIFEnum.values());
		Set<SIFInteraction> inters = searcher.searchSIF(model_urea);
		Set<Pathway> pathways = new HashSet<>();
		for (SIFInteraction inter : inters)
		{
			pathways.addAll(inter.getPathways());
		}

		assertFalse(pathways.isEmpty());

		Set<String> dataSources = new HashSet<>();
		for (SIFInteraction inter : inters)
		{
			dataSources.addAll(inter.getDataSources());
		}

		assertFalse(dataSources.isEmpty());
	}

	@Test
	public void getDataSources()
	{
		SIFSearcher searcher = new SIFSearcher(SIFEnum.values());
		Set<SIFInteraction> inters = searcher.searchSIF(model_urea);
		Set<String> dataSources = new HashSet<>();
		for (SIFInteraction inter : inters) {
			dataSources.addAll(inter.getDataSources());
		}
		assertFalse(dataSources.isEmpty());
	}

	@Test
	public void adjacencyMatrix()
	{
		SIFSearcher searcher = new SIFSearcher(SIFEnum.CONTROLS_STATE_CHANGE_OF,
			SIFEnum.CHEMICAL_AFFECTS);
		AdjacencyMatrix matrix = searcher.searchSIFGetMatrix(model_P53);
		//log.info("\n" + matrix);
		assertTrue(matrix.names.length > 0);
	}

	@Test
	public void withSIFMiner()
	{
		SIFSearcher s = new SIFSearcher(SIFEnum.CONTROLS_STATE_CHANGE_OF, SIFEnum.IN_COMPLEX_WITH);
		Set<SIFInteraction> sif = s.searchSIF(model_P53);
		assertFalse(sif.isEmpty());

		Set<String> pubmedIDs = new HashSet<>();
		for (SIFInteraction si : sif) {
			pubmedIDs.addAll(si.getPublicationIDs(true));
		}

		assertFalse(pubmedIDs.isEmpty());

		s = new SIFSearcher(SIFEnum.CATALYSIS_PRECEDES);
		sif = s.searchSIF(model_P53);
		assertTrue(sif.isEmpty());
	}

	@Test
	@Disabled
	public void oldFormatWriter() throws FileNotFoundException
	{
		SIFSearcher s = new SIFSearcher(SIFEnum.CONTROLS_STATE_CHANGE_OF, SIFEnum.IN_COMPLEX_WITH);
		SimpleIOHandler handler = new SimpleIOHandler();
		Model model = handler.convertFromOWL(new FileInputStream("/home/ozgun/Downloads/AR.TP53.owl"));
		Set<SIFInteraction> sif = s.searchSIF(model);
		ExtendedSIFWriter.write(sif, new FileOutputStream("temp.sif"));
		s.searchSIF(model, new FileOutputStream("output.sif"));
	}

	@Test
	@Disabled
	public void generateLargeSIFGraph() throws IOException
	{
		String dir = "/home/ozgun/Downloads/";
		SimpleIOHandler h = new SimpleIOHandler();
		String name = "testdata";
		Model model = h.convertFromOWL(new FileInputStream(dir + name + ".owl.gz"));

//		BlacklistGenerator gen = new BlacklistGenerator();
//		Blacklist blacklist = gen.generateBlacklist(model);
//		blacklist.write(new FileOutputStream(dir + "blacklist.txt"));
		Blacklist blacklist = new Blacklist(dir + "blacklist.txt");
		CommonIDFetcher idFetcher = new CommonIDFetcher();
		idFetcher.setUseUniprotIDs(false);
		SIFSearcher s = new SIFSearcher(idFetcher, SIFEnum.CONTROLS_STATE_CHANGE_OF);

//		SIFSearcher s = new SIFSearcher(idFetcher, SIFEnum.values());
//		SIFMiner[] miners = {new ControlsStateChangeOfMiner(), new CSCOButIsParticipantMiner(),
//			new CSCOThroughDegradationMiner(), new CSCOThroughControllingSmallMoleculeMiner(),
//			new ControlsExpressionMiner(), new ControlsExpressionWithConvMiner()};
//		for (SIFMiner miner : miners) miner.setBlacklist(blacklist);
//		SIFSearcher s = new SIFSearcher(miners);

		s.setBlacklist(blacklist);
		confirmPresenceOfUbiques(model, blacklist);
		long start = System.currentTimeMillis();
		s.searchSIF(model, new FileOutputStream(dir + name + ".sif"));
		log.info("Completed in: " + getPrintable(System.currentTimeMillis() - start));
	}

	@Test
	@Disabled
	public void generateSomeSIF() throws IOException
	{
		long start = System.currentTimeMillis();
		
		String dir = "/home/igor/Downloads/";
		SimpleIOHandler h = new SimpleIOHandler();
		Model model = h.convertFromOWL(new GZIPInputStream(new FileInputStream(dir + "pc14test.owl.gz")));

//		CommonIDFetcher idFetcher = new CommonIDFetcher();
//		idFetcher.setUseUniprotIDs(false);
//		SIFMiner miner = new CSCOThroughBindingSmallMoleculeMiner() {
//			@Override
//			public Pattern constructPattern()
//			{
//				Pattern pattern = super.constructPattern();
//				pattern.add(new IDConstraint(Collections.singleton("bioregistry.io/uniprot:Q9NVZ3")), "upper controller ER");
//				return pattern;
//			}
//		};
//		SIFSearcher s = new SIFSearcher(idFetcher, miner);
//		//BlacklistGenerator gen = new BlacklistGenerator();
//		//Blacklist blacklist = gen.generateBlacklist(model);
//		Blacklist blacklist = new Blacklist(dir + "blacklist.txt");
//		s.setBlacklist(blacklist);
//		//s.setBlacklist(new Blacklist("blacklist.txt"));

		ConfigurableIDFetcher idFetcher = new ConfigurableIDFetcher()
				.chemDbStartsWithOrEquals("chebi").seqDbStartsWithOrEquals("hgnc");
		SIFSearcher s = new SIFSearcher(idFetcher, SIFEnum.values());

		s.searchSIF(model, new FileOutputStream(dir + "temp.sif"), new CustomFormat(OutputColumn.Type.PATHWAY.name()));
		//the result file must have this (after the fix, CHEBI: banana+peel is used :))
		//CHEBI:28	used-to-produce	CHEBI:422	Glycolysis Pathway
		long time = System.currentTimeMillis() - start;
		log.info("Completed in: " + getPrintable(time));
	}

	@Test
	@Disabled
	public void countRelations() throws FileNotFoundException
	{
		String file = "/home/ozgun/Downloads/ctd_test.sif";
		Map<String, Integer> count = new HashMap<>();
		for (SIFEnum type : SIFEnum.values())
		{
			count.put(type.getTag(), 0);
		}
		Scanner sc = new Scanner(new File(file));
		while (sc.hasNextLine())
		{
			String rel = sc.nextLine().split("\t")[1];
			count.put(rel, count.get(rel) + 1);
		}
		for (SIFEnum type : SIFEnum.values())
		{
			log.info(count.get(type.getTag()) + "\t" + type);
		}
	}

	private static String getPrintable(long time)
	{
		int div = 1000 * 60 * 60;
		int hours = (int) (time / div);
		time %= div;
		div /= 60;
		int minutes = (int) (time / div);
		time %= div;
		div /= 60;
		int seconds = (int) (time / div);

		String s = hours > 0 ? hours + "h, " : "";
		s += minutes > 0 ? minutes + "m, " : "";
		s += seconds + "s";
		return s;
	}

	private void confirmPresenceOfUbiques(Model model, Blacklist blacklist)
	{
		int present = 0;
		int absent = 0;
		for (String ubique : blacklist.getListed())
		{
			if (model.getByID(ubique) != null) present++;
			else
			{
				absent++;
			}
		}
		log.info("absent ubique  = " + absent);
		log.info("present ubique = " + present);
	}

	@Test
	public void sifSearcher()
	{
		// Test CommonIDFetcher vs. ConfigurableIDFetcher vs. SimpleIDFetcher SIF seatch output results.
		final SIFType[] sifTypes = new SIFType[]{SIFEnum.IN_COMPLEX_WITH};
		final SIFSearcher commonSifSearcher = new SIFSearcher(null, sifTypes); //using default CommonIDFetcher
		final SIFSearcher simpleSifSearcher = new SIFSearcher(new SimpleIDFetcher(), sifTypes);
		// Configure an IDFetcher to collects HGNC Symbols (for seq. entities), or names (molecules), or URIs (fallback)
		IDFetcher configurableIDFetcher = new ConfigurableIDFetcher().seqDbStartsWithOrEquals("hgnc").useNameWhenNoDbMatch(true);
		final SIFSearcher customSifSearcher = new SIFSearcher(configurableIDFetcher, sifTypes);

		// Make a simple model with one interaction/complex, two participants
		Model model = BioPAXLevel.L3.getDefaultFactory().createModel();
		Complex c = model.addNew(Complex.class, "complex");
		c.setDisplayName("KRAS-PIK3"); //perhaps, not a real thing
		Protein p = model.addNew(Protein.class, "kras");
		p.setDisplayName("RAS");
		Protein pp = model.addNew(Protein.class, "pik3");
		pp.setDisplayName("PIK3");
		c.addComponent(p);
		c.addComponent(pp);

		// Test searcher.searchSIF(model) - check no. interactions, not empty, etc...
		Set<SIFInteraction> sifInteractions = customSifSearcher.searchSIF(model);
		assertTrue(sifInteractions.isEmpty()); //no xrefs, no entity references
		sifInteractions = commonSifSearcher.searchSIF(model);
		assertTrue(sifInteractions.isEmpty()); //no xrefs, no entity references

		//adding xrefs to PEs only does not help inferring the SIF interaction
		Xref prx = model.addNew(RelationshipXref.class,"prx");
		prx.setDb("HGNC Symbol");
		prx.setId("KRAS");
		p.addXref(prx);
		Xref pprx = model.addNew(RelationshipXref.class,"pprx");
		pprx.setDb("HGNC Symbol");
		pprx.setId("PIK3C3");
		//note: if xref.id were unknown/misspelled, tests would pass anyway (ER's name will be used instead) ;)
		pp.addXref(pprx);
		sifInteractions = commonSifSearcher.searchSIF(model);
		assertTrue(sifInteractions.isEmpty()); //still no result (due to - no ERs?)
		sifInteractions = customSifSearcher.searchSIF(model);
		assertTrue(sifInteractions.isEmpty());

		//let's add entity references without any xrefs yet -
		ProteinReference ppr = model.addNew(ProteinReference.class,"pik3_ref");
		ppr.setDisplayName("PIK3 family");
		pp.setEntityReference(ppr);
		ProteinReference pr = model.addNew(ProteinReference.class,"ras_ref");
		pr.setDisplayName("RAS family");
		p.setEntityReference(pr);
		assertEquals(7, model.getObjects().size());

		sifInteractions = customSifSearcher.searchSIF(model);
		assertFalse(sifInteractions.isEmpty());
		assertEquals(1, sifInteractions.size());
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		customSifSearcher.searchSIF(model, bos);
		assertEquals("PIK3 family\tin-complex-with\tRAS family", bos.toString());
		//log.info(bos.toString());

		//commonSifSearcher now also gets the same result after having recently being modified to use names when no xrefs found
		sifInteractions = commonSifSearcher.searchSIF(model);
		assertFalse(sifInteractions.isEmpty()); //OK
		assertEquals(1, sifInteractions.size());
		bos = new ByteArrayOutputStream();
		commonSifSearcher.searchSIF(model, bos);
		assertEquals("pik3_ref\tin-complex-with\tras_ref", bos.toString());
		//log.info(bos.toString());

		//using SimpleIDFetcher gets the result
		sifInteractions = simpleSifSearcher.searchSIF(model);
		assertFalse(sifInteractions.isEmpty()); //OK
		assertEquals(1, sifInteractions.size());
		bos = new ByteArrayOutputStream();
		simpleSifSearcher.searchSIF(model, bos);
		assertEquals("pik3_ref\tin-complex-with\tras_ref", bos.toString());
		//log.info(bos.toString());

		//add xrefs to Ers and repeat the SIF export
		ppr.addXref(pprx);
		pr.addXref(prx);

		sifInteractions = customSifSearcher.searchSIF(model);
		assertFalse(sifInteractions.isEmpty());
		assertEquals(1, sifInteractions.size()); //OK
		bos = new ByteArrayOutputStream();
		customSifSearcher.searchSIF(model, bos);
		assertEquals("KRAS\tin-complex-with\tPIK3C3", bos.toString());
		//log.info(bos.toString());

		sifInteractions = commonSifSearcher.searchSIF(model);
		assertFalse(sifInteractions.isEmpty());
		assertEquals(1, sifInteractions.size()); //OK
		bos = new ByteArrayOutputStream();
		commonSifSearcher.searchSIF(model, bos);
		assertEquals("pik3_ref\tin-complex-with\tras_ref", bos.toString());
		//log.info(bos.toString());

		//TODO add tests using .seqDbStartsWithOrEquals with:
		// "hgnc","uniprot","ncbi","mirbase", and .chemDbStartsWithOrEquals("chebi")
	}

}
