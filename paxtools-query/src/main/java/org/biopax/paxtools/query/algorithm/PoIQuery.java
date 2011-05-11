package org.biopax.paxtools.query.algorithm;


import org.biopax.paxtools.query.model.GraphObject;
import org.biopax.paxtools.query.model.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Finds the paths from a specified source set of states or entities to a
 * specified target set of states or entities within the boundaries of a
 * specified length limit. Takes source set, target set, type of distance
 * limit, distance limit and strict value. Based on these parameters, the
 * interactions within source set and within target set may be omitted and/or
 * "shortest+k" may be used as length limit.
 *
 * @author Ozgun Babur
 * @author Merve Cakir
 */
public class PoIQuery
{
	/**
	 * The set of nodes from which the paths of interests should start.
	 */
	private Set<Node> sourceSet;

	/**
	 * The set of nodes to which the paths of interests should arrive.
	 */
	private Set<Node> targetSet;

	/**
	 * True if length limit is used, false if shortes+k is used.
	 */
	private LimitType limitType;

	/**
	 * Based on the limitType, given integer may be used directly as stop
	 * distance or may be added up with the shortest path's length and used as
	 * stop distance.
	 */
	private int stopDistance;

	/**
	 * When true, the interactions within source set and within target set are
	 * not involved in result set.
	 */
	private boolean strict;

	/**
	 * This is a hard-coded limit to use with the shortest_plus_k limit. If there is no shortest
	 * path between the given nodes, the algorithm should not try to traverse all the graph. So this
	 * shortest path search limit will make sure that the algorithm will not search for indefinitely
	 * for the shortest path.
	 */
	private static final int LIMIT_FOR_SP_SEARCH = 25;

	/**
	 * Constructor
	 */
	public PoIQuery(Set<Node> sourceSet,
		Set<Node> targetSet,
		LimitType limitType,
		int stopDistance,
		boolean strict)
	{
		assert limitType != null : "limitType should be specified";

		this.sourceSet = sourceSet;
		this.targetSet = targetSet;
		this.limitType = limitType;
		this.stopDistance = stopDistance;
		this.strict = strict;
	}

	public Set<GraphObject> run()
	{
		/**
		 * Candidate contains all the graph objects that are the results of BFS.
		 * Eliminating nodes from candidate according to their labels will
		 * yield result.
		 */
		Map<GraphObject, Integer> candidate = new HashMap<GraphObject, Integer>();
		Set<GraphObject> result = new HashSet<GraphObject>();

		BFS bfsFwd = null;
		BFS bfsRev = null;

		if (limitType == LimitType.NORMAL && !strict)
		{
			bfsFwd = new BFS(sourceSet, null, Direction.DOWNSTREAM, stopDistance);
			bfsRev = new BFS(targetSet, null, Direction.UPSTREAM, stopDistance);
		}
		else if (limitType == LimitType.NORMAL && strict)
		{
			bfsFwd = new BFS(sourceSet, targetSet, Direction.DOWNSTREAM, stopDistance);
			bfsRev = new BFS(targetSet, sourceSet, Direction.UPSTREAM, stopDistance);
		}
		else if (limitType == LimitType.SHORTEST_PLUS_K && !strict)
		{
			bfsFwd = new BFS(sourceSet, null, Direction.DOWNSTREAM, LIMIT_FOR_SP_SEARCH);
			bfsRev = new BFS(targetSet, null, Direction.UPSTREAM, LIMIT_FOR_SP_SEARCH);
		}
		else if (limitType == LimitType.SHORTEST_PLUS_K && strict)
		{
			bfsFwd = new BFS(sourceSet, targetSet, Direction.DOWNSTREAM, LIMIT_FOR_SP_SEARCH);
			bfsRev = new BFS(targetSet, sourceSet, Direction.UPSTREAM, LIMIT_FOR_SP_SEARCH);
		}

		candidate.putAll(bfsFwd.run());
		candidate.putAll(bfsRev.run());

		int limit = stopDistance;

		if(limitType == LimitType.NORMAL)
		{
			/**
			 * Only the graph objects whose sum of two search labels being
			 * smaller than or equal to the distance limit will be in the result.
			 */
			for (GraphObject go : candidate.keySet())
			{
				if ((bfsFwd.getLabel(go) + bfsRev.getLabel(go)) <= limit)
				{
					result.add(go);
				}
			}
		}
		else
		{
			int shortestPath = Integer.MAX_VALUE;

			/**
			 * Summing up the labels of two search will give the length of the
			 * path that passes through that particular graph object and the
			 * minimum of those lengths will be the length of the shortest path.
			 */
			for (GraphObject go : candidate.keySet())
			{
				if ((bfsFwd.getLabel(go) + bfsRev.getLabel(go)) <= shortestPath)
				{
					shortestPath = (bfsFwd.getLabel(go) + bfsRev.getLabel(go));
				}
			}

			limit = shortestPath + stopDistance;

			// Proceed only if there is a shortest path found

			if (shortestPath < Integer.MAX_VALUE / 2)
			{
				/**
				 * Only the graph objects whose sum of two search labels being
				 * smaller than or equal to the "shortest + limit" will be in the
				 * result.
				 */
				for (GraphObject go : candidate.keySet())
				{
					if ((bfsFwd.getLabel(go) + bfsRev.getLabel(go)) <= limit)
					{
						result.add(go);
					}
				}
			}
		}

		Set<Node> ST = new HashSet<Node>(sourceSet);
		ST.addAll(targetSet);

		CycleBreaker breaker = new CycleBreaker(result, ST, limit);
		breaker.breakCycles();

		Prune prune = new Prune(result, ST);
		prune.run();

		return result;
	}
}
