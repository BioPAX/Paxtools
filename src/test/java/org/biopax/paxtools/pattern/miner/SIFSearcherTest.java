package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.pattern.PatternBoxTest;
import org.junit.Assert;
import org.junit.Test;

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
}
