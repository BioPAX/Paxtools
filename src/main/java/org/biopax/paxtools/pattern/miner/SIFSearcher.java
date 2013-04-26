package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Searcher;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Searches a model and generates SIF network using the pattern matches.
 *
 * @author Ozgun Babur
 */
public class SIFSearcher
{
	/**
	 * SIF miners to use.
	 */
	private SIFMiner[] miners;

	/**
	 * Constructor with miners.
	 * @param miners sif miners
	 */
	public SIFSearcher(SIFMiner... miners)
	{
		this.miners = miners;
	}

	/**
	 * Searches the given model with the contained miners.
	 * @param model model to search
	 * @return sif interactions
	 */
	public Set<SIFInteraction> searchSIF(Model model)
	{
		Set<SIFInteraction> set = new HashSet<SIFInteraction>();

		for (SIFMiner miner : miners)
		{
			Map<BioPAXElement,List<Match>> matches = Searcher.search(model, miner.getPattern());

			for (List<Match> matchList : matches.values())
			{
				for (Match m : matchList)
				{
					SIFInteraction sif = miner.createSIFInteraction(m);

					if (sif != null)
					{
						set.add(sif);
					}
				}
			}
		}
		return set;
	}
}
