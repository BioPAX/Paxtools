package org.biopax.paxtools.pattern.miner;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.Searcher;

import java.util.*;

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
		Map<SIFInteraction, SIFInteraction> map = new HashMap<SIFInteraction, SIFInteraction>();

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
						if (map.containsKey(sif))
						{
							SIFInteraction existing = map.get(sif);
							existing.mergeWith(sif);
						}
						else map.put(sif, sif);
					}
				}
			}
		}
		return new HashSet<SIFInteraction>(map.values());
	}
}
