package org.biopax.paxtools.causality.analysis;

import java.util.*;

/**
 */
public class ScorePropagator
{
	public Map<String, Double> propagate(Map<String, Double> init, Traverse st, int limit,
		double propagationMultiplier)
	{
		Map<String, Double> score = new HashMap<String, Double>();
		for (String seed : init.keySet())
		{
			if (!score.containsKey(seed)) score.put(seed, 0D);

			// Transfer initial weight of the seed to the score
			score.put(seed, score.get(seed) + init.get(seed));

			Set<String> scored = new HashSet<String>(Arrays.asList(seed));
			Set<String> visitedDown = new HashSet<String>(scored);
			Set<String> visitedUp = new HashSet<String>(scored);
			Set<String> seedDown = new HashSet<String>(scored);
			Set<String> seedUp = new HashSet<String>(scored);
			
			for (int i = 0; i < limit; i++)
			{
				seedDown = st.goBFS(seedDown, visitedDown, true);
				for (String n : seedDown)
				{
					if (scored.contains(n)) continue;
					if (!score.containsKey(n)) score.put(n, 0D);
					score.put(n, score.get(n) + 
						(init.get(seed) * Math.pow(propagationMultiplier, i+1)));
				}
				scored.addAll(seedDown);
				visitedDown.addAll(seedDown);

				seedUp = st.goBFS(seedUp, visitedUp, false);
				for (String n : seedUp)
				{
					if (scored.contains(n)) continue;
					if (!score.containsKey(n)) score.put(n, 0D);
					score.put(n, score.get(n) + 
						(init.get(seed) * Math.pow(propagationMultiplier, i+1)));
				}
				scored.addAll(seedUp);
				visitedUp.addAll(seedUp);
			}
		}
		return score;
	}

	public static void main(String[] args)
	{

	}
}
