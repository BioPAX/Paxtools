package org.biopax.paxtools.causality.analysis;

import org.biopax.paxtools.causality.util.Histogram;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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

	public Map<String, Map<String, Double>> loadScores() throws IOException
	{
		Map<String, Map<String, Double>> map = new HashMap<String, Map<String, Double>>();
		BufferedReader reader = new BufferedReader(new FileReader("/home/ozgun/Desktop/scores.txt"));

		String line = reader.readLine();
		String[] drug = line.split("\t");
		for (String d : drug)
		{
			map.put(d, new HashMap<String, Double>());
		}
		
		for (line = reader.readLine(); line != null; line = reader.readLine())
		{
			String[] token = line.split("\t");
			String gene = token[0];

			for (int i = 1; i < token.length; i++)
			{
				double v = Double.parseDouble(token[i]);
				map.get(drug[i-1]).put(gene, v > 0 ? v : 0);
			}
		}

		reader.close();
		return map;
	}
	
	public static void main(String[] args) throws IOException
	{
		ScorePropagator prop = new ScorePropagator();
		Map<String, Map<String, Double>> map = prop.loadScores();
		String drug = "Gefitinib";
		System.out.println("map.size() = " + map.size());
		System.out.println("map.size() = " + map.get(drug).size());
		Traverse trav = new Traverse();
		trav.load("/home/ozgun/Desktop/SIF.txt", new HashSet<String>(Arrays.asList("BINDS_TO")),
			new HashSet<String>(Arrays.asList("STATE_CHANGE", "TRANSCRIPTION", "DEGRADATION")));
		final Map<String, Double> scores = prop.propagate(map.get(drug), trav, 2, 0.1);
		System.out.println("scores.size() = " + scores.size());
		List<String> genes = new ArrayList<String>(scores.keySet());
		Collections.sort(genes, new Comparator<String>()
		{
			@Override
			public int compare(String s1, String s2)
			{
				return scores.get(s2).compareTo(scores.get(s1));
			}
		});
		for (int i = 0; i < 10; i++)
		{
			String g = genes.get(i);
			System.out.print(g);
			System.out.print("\t" + scores.get(g));
			System.out.print("\t" + map.get(drug).get(g));
			System.out.println("\t" + trav.getDegree(g));
		}

//		Histogram h = new Histogram(10);
//		for (Double v : scores.values())
//		{
//			h.count(v);
//		}
//		h.print();
	}
}
