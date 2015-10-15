package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.PatternBoxTest;
import org.biopax.paxtools.pattern.util.AdjacencyMatrix;
import org.biopax.paxtools.pattern.util.Blacklist;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.util.*;

import static org.junit.Assert.*;

/**
 * TODO replace temporary/ignored "tests" (hard-coded local input paths) with normal test resources and assertions.
 *
 * @author Ozgun Babur
 */
public class SIFSearcherTest extends PatternBoxTest
{
	@Test
	public void testGetPathways()
	{
		SIFSearcher searcher = new SIFSearcher(SIFEnum.values());
		Set<SIFInteraction> inters = searcher.searchSIF(model_urea);
		Set<Pathway> pathways = new HashSet<Pathway>();
		for (SIFInteraction inter : inters)
		{
			pathways.addAll(inter.getPathways());
		}

		Assert.assertFalse(pathways.isEmpty());

		Set<String> dataSources = new HashSet<String>();
		for (SIFInteraction inter : inters)
		{
			dataSources.addAll(inter.getDataSources());
		}

		Assert.assertFalse(dataSources.isEmpty());
	}

	@Test
	public void testGetDataSources()
	{
		SIFSearcher searcher = new SIFSearcher(SIFEnum.values());
		Set<SIFInteraction> inters = searcher.searchSIF(model_urea);
		Set<String> dataSources = new HashSet<String>();
		for (SIFInteraction inter : inters)
		{
			dataSources.addAll(inter.getDataSources());
		}

		Assert.assertFalse(dataSources.isEmpty());
	}

	@Test
	public void testAdjacencyMatrix()
	{
		SIFSearcher searcher = new SIFSearcher(SIFEnum.CONTROLS_STATE_CHANGE_OF,
			SIFEnum.CHEMICAL_AFFECTS);
		AdjacencyMatrix matrix = searcher.searchSIFGetMatrix(model_P53);
		System.out.println(matrix);
		Assert.assertTrue(matrix.names.length > 0);
	}

	@Test
	@Ignore
	public void testSIFMiner()
	{
		SIFSearcher s = new SIFSearcher(SIFEnum.CONTROLS_STATE_CHANGE_OF, SIFEnum.IN_COMPLEX_WITH);
		Set<SIFInteraction> sif = s.searchSIF(model_P53);
		Assert.assertFalse(sif.isEmpty());

		Set<String> pubmedIDs = new HashSet<String>();
		for (SIFInteraction si : sif)
		{
			pubmedIDs.addAll(si.getPubmedIDs());
		}

		Assert.assertFalse(pubmedIDs.isEmpty());

		s = new SIFSearcher(SIFEnum.CATALYSIS_PRECEDES);
		sif = s.searchSIF(model_P53);
		Assert.assertTrue(sif.isEmpty());
	}

	@Test
	@Ignore
	public void testOldFormatWriter() throws FileNotFoundException
	{
		SIFSearcher s = new SIFSearcher(SIFEnum.CONTROLS_STATE_CHANGE_OF, SIFEnum.IN_COMPLEX_WITH);
		SimpleIOHandler handler = new SimpleIOHandler();
		Model model = handler.convertFromOWL(new FileInputStream("/home/ozgun/Desktop/AR.TP53.owl"));
		Set<SIFInteraction> sif = s.searchSIF(model);
		OldFormatWriter.write(sif, new FileOutputStream("temp.sif"));
	}

	@Test
	@Ignore
	public void generateLargeSIFGraph() throws IOException
	{
//		BPCollections.I.setProvider(new TProvider());

//		String dir = "/home/ozgun/Projects/biopax-pattern/";
		String dir = "/home/ozgun/Downloads/PC-resources/";
		SimpleIOHandler h = new SimpleIOHandler();
		String name = "Pathway Commons.7.Reactome.BIOPAX";
//		String name = "temp";
		Model model = h.convertFromOWL(new FileInputStream(dir + name + ".owl"));

//		BlacklistGenerator gen = new BlacklistGenerator();
//		Blacklist blacklist = gen.generateBlacklist(model);
//		blacklist.write(new FileOutputStream(dir + "blacklist.txt"));
		Blacklist blacklist = new Blacklist(dir + "blacklist.txt");

		CommonIDFetcher idFetcher = new CommonIDFetcher();
		idFetcher.setUseUniprotIDs(false);
		SIFSearcher s = new SIFSearcher(idFetcher, SIFEnum.values());
//		SIFMiner[] miners = {new ControlsStateChangeOfMiner(), new CSCOButIsParticipantMiner(),
//			new CSCOThroughDegradationMiner(), new CSCOThroughControllingSmallMoleculeMiner(),
//			new ControlsExpressionMiner(), new ControlsExpressionWithConvMiner()};
//		for (SIFMiner miner : miners) miner.setBlacklist(blacklist);

//		SIFSearcher s = new SIFSearcher(miners);
		s.setBlacklist(blacklist);

		confirmPresenceOfUbiques(model, blacklist);

		long start = System.currentTimeMillis();
		s.searchSIF(model, new FileOutputStream(dir + name + ".sif"), false);

		long time = System.currentTimeMillis() - start;
		System.out.println("Completed in: " + getPrintable(time));
		Assert.assertTrue(2 + 2 == 4);
	}

	@Test
	@Ignore
	public void generateSomeSIFGraph() throws IOException
	{
		long start = System.currentTimeMillis();

//		BPCollections.I.setProvider(new TProvider());

//		String dir = "/home/ozgun/Projects/biopax-pattern/";
		String dir = "/home/ozgun/Desktop/";
		SimpleIOHandler h = new SimpleIOHandler();
		Model model = h.convertFromOWL(new FileInputStream(dir + "temp.owl"));

		CommonIDFetcher idFetcher = new CommonIDFetcher();
		idFetcher.setUseUniprotIDs(true);
		SIFSearcher s = new SIFSearcher(idFetcher, SIFEnum.IN_COMPLEX_WITH);

		BlacklistGenerator gen = new BlacklistGenerator();
		Blacklist blacklist = gen.generateBlacklist(model);
		s.setBlacklist(blacklist);
//		s.setBlacklist(new Blacklist("blacklist.txt"));
		s.searchSIF(model, new FileOutputStream(dir + "temp.sif"), true);

		long time = System.currentTimeMillis() - start;
		System.out.println("Completed in: " + getPrintable(time));
		Assert.assertTrue(2 + 2 == 4);
	}

	@Test
	@Ignore
	public void countRelations() throws FileNotFoundException
	{
		String file = "/home/ozgun/Downloads/PC-resources/Pathway Commons.7.Comparative Toxicogenomics Database.BIOPAX.sif";

		Map<String, Integer> count = new HashMap<String, Integer>();
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
			System.out.println(count.get(type.getTag()) + "\t" + type);
		}
		Assert.assertTrue(2 + 2 == 4);
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
		System.out.println("absent ubique  = " + absent);
		System.out.println("present ubique = " + present);
	}


	@Test
	@Ignore
	public void testSIFSearcher2() throws IOException
	{
//		generate("/home/ozgun/Projects/biopax-pattern/All-Human-Data.owl",
//				"/home/ozgun/Projects/biopax-pattern/ubiquitous-ids.txt", "SIF.txt");
	}

	@Test
	public void testSIFSearcher() throws IOException
	{
		// Test CommonIdFetcher vs. NamedIDFetcher SIF seatch output results.
		final SIFType[] sifTypes = new SIFType[]{SIFEnum.IN_COMPLEX_WITH};
		final SIFSearcher commonSifSearcher = new SIFSearcher(null, sifTypes); //CommonIDFetcher is used by def.
		final SIFSearcher namedSifSearcher = new SIFSearcher(new NamedIDFetcher(), sifTypes);
		final SIFSearcher simpleSifSearcher = new SIFSearcher(new SimpleIDFetcher(), sifTypes);

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
		Set<SIFInteraction> sifInteractions = namedSifSearcher.searchSIF(model);
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
		sifInteractions = namedSifSearcher.searchSIF(model);
		assertTrue(sifInteractions.isEmpty());

		//let's add entity references without any xrefs yet -
		ProteinReference ppr = model.addNew(ProteinReference.class,"pik3_ref");
		ppr.setDisplayName("PIK3 family");
		pp.setEntityReference(ppr);
		ProteinReference pr = model.addNew(ProteinReference.class,"ras_ref");
		pr.setDisplayName("RAS family");
		p.setEntityReference(pr);

		assertEquals(7, model.getObjects().size());

		sifInteractions = namedSifSearcher.searchSIF(model);
		assertFalse(sifInteractions.isEmpty());
		assertEquals(1, sifInteractions.size());
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		namedSifSearcher.searchSIF(model, bos);
		System.out.println(bos.toString());//prints (using names): "PIK3 family	in-complex-with	RAS family"
		//commonSifSearcher now also gets the same result after having recently being modified to use names when no xrefs found
		sifInteractions = commonSifSearcher.searchSIF(model);
		assertFalse(sifInteractions.isEmpty()); //OK
		assertEquals(1, sifInteractions.size());
		bos = new ByteArrayOutputStream();
		commonSifSearcher.searchSIF(model, bos);
		System.out.println(bos.toString());

		//using SimpleIDFetcher gets the result
		sifInteractions = simpleSifSearcher.searchSIF(model);
		assertFalse(sifInteractions.isEmpty()); //OK
		assertEquals(1, sifInteractions.size());
		bos = new ByteArrayOutputStream();
		simpleSifSearcher.searchSIF(model, bos);
		System.out.println(bos.toString());

		//add xrefs to Ers and repeat the SIF export
		ppr.addXref(pprx);
		pr.addXref(prx);
//		bos = new ByteArrayOutputStream();
//		new SimpleIOHandler().convertToOWL(model,bos);
//		System.out.println(bos.toString());

		sifInteractions = namedSifSearcher.searchSIF(model);
		assertFalse(sifInteractions.isEmpty());
		assertEquals(1, sifInteractions.size()); //OK
		bos = new ByteArrayOutputStream();
		namedSifSearcher.searchSIF(model, bos);
		System.out.println(bos.toString());

		sifInteractions = commonSifSearcher.searchSIF(model);
		assertFalse(sifInteractions.isEmpty());
		assertEquals(1, sifInteractions.size()); //OK
		bos = new ByteArrayOutputStream();
		commonSifSearcher.searchSIF(model, bos);
		System.out.println(bos.toString());

		//TODO more...
	}

	public static void generate(String inputModelFile, String ubiqueIDFile, String outputFile)
		throws IOException
	{
		SimpleIOHandler h = new SimpleIOHandler();
		Model model = h.convertFromOWL(new FileInputStream(inputModelFile));

		List<SIFInteraction> sifs = new ArrayList<SIFInteraction>(generate(model,
			new Blacklist(ubiqueIDFile)));

		Collections.sort(sifs);

		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

		for (SIFInteraction sif : sifs)
		{
			writer.write(sif + "\n");
		}

		writer.close();
	}

	public static Set<SIFInteraction> generate(Model model, Blacklist blacklist)
	{
		SIFSearcher searcher = new SIFSearcher(SIFEnum.CONTROLS_STATE_CHANGE_OF);
//			SIFType.CONTROLS_EXPRESSION, SIFType.CONTROLS_DEGRADATION);

		searcher.setBlacklist(blacklist);

		return searcher.searchSIF(model);
	}

}
