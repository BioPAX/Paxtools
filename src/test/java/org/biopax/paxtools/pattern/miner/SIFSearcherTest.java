package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.pattern.PatternBoxTest;
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
	@Ignore
	public void testSIFMiner()
	{
		SIFSearcher s = new SIFSearcher(SIFType.CONTROLS_STATE_CHANGE_OF, SIFType.IN_COMPLEX_WITH);
		Set<SIFInteraction> sif = s.searchSIF(model);
		Assert.assertFalse(sif.isEmpty());

		Set<String> pubmedIDs = new HashSet<String>();
		for (SIFInteraction si : sif)
		{
			pubmedIDs.addAll(si.getPubmedIDs());
		}

		Assert.assertFalse(pubmedIDs.isEmpty());

		s = new SIFSearcher(SIFType.CATALYSIS_PRECEDES);
		sif = s.searchSIF(model);
		Assert.assertTrue(sif.isEmpty());
	}

	@Test
	@Ignore
	public void generateLargeSIFGraph() throws IOException
	{
		SimpleIOHandler h = new SimpleIOHandler();
		Model model = h.convertFromOWL(new FileInputStream
				("/home/ozgun/Projects/pattern/All-Human-Data.owl"));

		SIFSearcher s = new SIFSearcher(SIFType.values());

		Set<String> ubiqueIDs = loadUbiqueIDs("/home/ozgun/Projects/pattern/blacklist.txt");
		s.setUbiqueIDs(ubiqueIDs);
		confirmPresenceOfUbiques(model, ubiqueIDs);
		Set<SIFInteraction> set = s.searchSIF(model);

		BufferedWriter writer = new BufferedWriter(
			new FileWriter("/home/ozgun/PC.sif"));

		for (SIFInteraction sif : set)
		{
			writer.write(sif.toString() + "\n");
		}

		writer.close();
	}

	private void confirmPresenceOfUbiques(Model model, Set<String> ubiques)
	{
		int present = 0;
		int absent = 0;
		for (String ubique : ubiques)
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

	public static void generate(String inputModelFile, String ubiqueIDFile, String outputFile) throws IOException
	{
		SimpleIOHandler h = new SimpleIOHandler();
		Model model = h.convertFromOWL(new FileInputStream(inputModelFile));

		List<SIFInteraction> sifs = new ArrayList<SIFInteraction>(
			generate(model, loadUbiqueIDs(ubiqueIDFile)));

		Collections.sort(sifs);

		BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

		for (SIFInteraction sif : sifs)
		{
			writer.write(sif + "\n");
		}

		writer.close();
	}

	public static Set<SIFInteraction> generate(Model model, Set<String> ubiqueIDs)
	{
		SIFSearcher searcher = new SIFSearcher(SIFType.CONTROLS_STATE_CHANGE_OF);
//			SIFType.CONTROLS_EXPRESSION, SIFType.CONTROLS_DEGRADATION);

		searcher.setUbiqueIDs(ubiqueIDs);

		return searcher.searchSIF(model);
	}

	private static Set<String> loadUbiqueIDs(String filename) throws FileNotFoundException
	{
		Set<String> ids = new HashSet<String>();
		Scanner scan = new Scanner(new File(filename));
		while (scan.hasNextLine())
		{
			String line = scan.nextLine();

			if (!line.isEmpty()) ids.add(line);
		}
		return ids;
	}
}
