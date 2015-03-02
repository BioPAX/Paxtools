package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.io.BioPAXIOHandler;
import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.Protein;
import org.biopax.paxtools.model.level3.SmallMolecule;
import org.biopax.paxtools.pattern.PatternBoxTest;
import org.biopax.paxtools.pattern.util.AdjacencyMatrix;
import org.biopax.paxtools.pattern.util.Blacklist;
import org.biopax.paxtools.trove.TProvider;
import org.biopax.paxtools.util.BPCollections;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.util.*;

/**
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
//		String name = "Pathway Commons.7.Comparative Toxicogenomics Database.BIOPAX";
		String name = "temp";
		Model model = h.convertFromOWL(new FileInputStream(dir + name + ".owl"));

//		BlacklistGenerator gen = new BlacklistGenerator();
//		Blacklist blacklist = gen.generateBlacklist(model);
//		blacklist.write(new FileOutputStream(dir + "blacklist.txt"));
		Blacklist blacklist = new Blacklist(dir + "blacklist.txt");

		CommonIDFetcher idFetcher = new CommonIDFetcher();
		idFetcher.setUseUniprotIDs(true);
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
	public void testSIFSearcher() throws IOException
	{
		generate("/home/ozgun/Projects/biopax-pattern/All-Human-Data.owl",
//		generate("/home/ozgun/Desktop/temp.owl",
				"/home/ozgun/Projects/biopax-pattern/ubiquitous-ids.txt", "SIF.txt");
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

	@Test
	@Ignore
	public void testTemp()
	{
		FileInputStream fin;
		try {
			fin = new FileInputStream("/home/ozgun/Downloads/testmodel.owl");
			BioPAXIOHandler handler = new SimpleIOHandler();
			Model model = handler.convertFromOWL(fin); //THIS LINE OF CODE CAUSES THE ERROR

			for (SmallMolecule sm : model.getObjects(SmallMolecule.class))
			{
				System.out.println("protein.getDisplayName() = " + sm.getName().iterator().next());
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
