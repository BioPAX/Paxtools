package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.io.SimpleIOHandler;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.pattern.PatternBoxTest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class SIFSearcherTest extends PatternBoxTest
{
	@Test
	public void testSIFMiner()
	{
		SIFSearcher s = new SIFSearcher(new ControlsStateChangeMiner(), new InSameComplexMiner());
		Set<SIFInteraction> sif = s.searchSIF(model);
		Assert.assertFalse(sif.isEmpty());

		Set<String> pubmedIDs = new HashSet<String>();
		for (SIFInteraction si : sif)
		{
			if (si.pubmedIDs != null) pubmedIDs.addAll(si.pubmedIDs);
		}

		Assert.assertFalse(pubmedIDs.isEmpty());

		s = new SIFSearcher(new ConsecutiveCatalysisMiner(null));
		sif = s.searchSIF(model);
		Assert.assertTrue(sif.isEmpty());
	}

	@Test
	@Ignore
	public void generateLargeSIFGraph() throws IOException
	{
		SimpleIOHandler h = new SimpleIOHandler();
		Model model = h.convertFromOWL(new FileInputStream("/home/ozgun/Desktop/PC.owl"));

		SIFSearcher s = new SIFSearcher(
			new ControlsStateChangeMiner(),
			new TranscriptionalRegulationMiner(),
			new DegradesMiner(),
			new ControlsStateChangeButIsParticipantMiner(),
			new TranscriptionalRegulationWithConvMiner());

		Set<SIFInteraction> set = s.searchSIF(model);

		BufferedWriter writer = new BufferedWriter(
			new FileWriter("/home/ozgun/Desktop/SIF_uniprot.txt"));

		for (SIFInteraction sif : set)
		{
			writer.write(sif.toString() + "\n");
		}

		writer.close();
	}
}
