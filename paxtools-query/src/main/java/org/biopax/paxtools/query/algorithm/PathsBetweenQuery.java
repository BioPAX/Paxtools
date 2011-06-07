package org.biopax.paxtools.query.algorithm;


import org.biopax.paxtools.query.model.GraphObject;
import org.biopax.paxtools.query.model.Node;

import java.util.*;

/**
 * Finds the paths between the specified source set of states within the boundaries of a
 * specified length limit.
 *
 * @author Ozgun Babur
 */
public class PathsBetweenQuery
{
	/**
	 * The set of nodes from which the paths of interests should start.
	 */
	private Collection<Set<Node>> sourceSet;

	/**
	 * Based on the limitType, given integer may be used directly as stop
	 * distance or may be added up with the shortest path's length and used as
	 * stop distance.
	 */
	private int limit;

	/**
	 * Constructor
	 */
	public PathsBetweenQuery(Collection<Set<Node>> sourceSet, int limit)
	{
		this.sourceSet = sourceSet;
		this.limit = limit;
	}

	public Set<GraphObject> run()
	{
		/**
		 * Distance labels of graph objects. Note that each source set may have a distinct label for
		 * the object.
		 */
		Map<GraphObject, Map<Set<Node>, Integer>> fwdObj = new HashMap<GraphObject, Map<Set<Node>, Integer>>();
		Map<GraphObject, Map<Set<Node>, Integer>> revObj = new HashMap<GraphObject, Map<Set<Node>, Integer>>();

		Set<GraphObject> result = new HashSet<GraphObject>();

		for (Set<Node> set : sourceSet)
		{
			BFS bfsFwd = new BFS(set, null, Direction.DOWNSTREAM, limit);
			BFS bfsRev = new BFS(set, null, Direction.UPSTREAM, limit);

			recordLabels(fwdObj, set, bfsFwd.run());
			recordLabels(revObj, set, bfsRev.run());
		}


		/**
		 * Only the graph objects whose sum of two search labels, coming from different sets,
		 * being smaller than or equal to the distance limit will be in the result.
		 */
		for (GraphObject go : fwdObj.keySet())
		{
			if (!revObj.containsKey(go)) continue;

			if (onTheResultPath(fwdObj.get(go), revObj.get(go)))
			{
				result.add(go);
			}
		}

		Set<Node> sources = new HashSet<Node>();
		for (Set<Node> set : sourceSet)
		{
			sources.addAll(set);
		}

		CycleBreaker breaker = new CycleBreaker(result, sources, limit);
		breaker.breakCycles();

		Prune prune = new Prune(result, sources);
		prune.run();

		return result;
	}

	private void recordLabels(Map<GraphObject, Map<Set<Node>, Integer>> labels, Set<Node> set,
		Map<GraphObject, Integer> bfsResult)
	{
		for (GraphObject go : bfsResult.keySet())
		{
			if (!labels.containsKey(go)) labels.put(go, new HashMap<Set<Node>, Integer>());

			labels.get(go).put(set, bfsResult.get(go));
		}
	}

	private boolean onTheResultPath(Map<Set<Node>, Integer> fwdMap, Map<Set<Node>, Integer> revMap)
	{
		for (Set<Node> set1 : fwdMap.keySet())
		{
			for (Set<Node> set2 : revMap.keySet())
			{
				if (set1 == set2) continue;

				int dist = fwdMap.get(set1) + revMap.get(set2);

				if (dist <= limit) return true;
			}
		}
		return false;
	}
}
