package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.pattern.PatternBoxTest;
import org.biopax.paxtools.pattern.util.Blacklist;
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
		long start = System.currentTimeMillis();

		SimpleIOHandler h = new SimpleIOHandler();
		Model model = h.convertFromOWL(new FileInputStream
				("/home/ozgun/Projects/biopax-pattern/All-Human-Data.owl"));

		BlacklistGenerator gen = new BlacklistGenerator();
		Blacklist blacklist = gen.generateBlacklist(model);

		SIFSearcher s = new SIFSearcher(SIFType.values());
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
		SIFSearcher searcher = new SIFSearcher(SIFType.CONTROLS_STATE_CHANGE_OF);
//			SIFType.CONTROLS_EXPRESSION, SIFType.CONTROLS_DEGRADATION);

		searcher.setBlacklist(blacklist);

		return searcher.searchSIF(model);
	}
}
