package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.Pathway;
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
		SIFSearcher searcher = new SIFSearcher(
			SIFEnum.CONTROLS_STATE_CHANGE_OF, SIFEnum.IN_COMPLEX_WITH);
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
		long start = System.currentTimeMillis();

		BPCollections.I.setProvider(new TProvider());

		SimpleIOHandler h = new SimpleIOHandler();
		Model model = h.convertFromOWL(new FileInputStream
				("/home/ozgun/Projects/biopax-pattern/All-Data.owl"));

		BlacklistGenerator gen = new BlacklistGenerator();
		Blacklist blacklist = gen.generateBlacklist(model);
		blacklist.write(new FileOutputStream("blacklist.txt"));

		SIFSearcher s = new SIFSearcher(SIFEnum.values());
		s.setBlacklist(blacklist);

		confirmPresenceOfUbiques(model, blacklist);
		s.searchSIF(model, new FileOutputStream("/home/ozgun/PC.sif"), true);

		long time = System.currentTimeMillis() - start;
		System.out.println("Completed in: " + getPrintable(time));
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
}
