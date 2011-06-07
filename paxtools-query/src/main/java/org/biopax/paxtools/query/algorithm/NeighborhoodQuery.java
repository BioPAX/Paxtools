package org.biopax.paxtools.query.algorithm;

import org.biopax.paxtools.query.model.GraphObject;
import org.biopax.paxtools.query.model.Node;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Searches the neighborhood of given source set of nodes
 * 
 * @author Shatlyk Ashyralyev
 */
public class NeighborhoodQuery
{
	/**
	 * Set of source nodes.
	 */
	private Set<Node> sourceNodes;
	
	/**
	 * Booleans to determine the directin of search.
	 */
	private Direction direction;

	/**
	 * Stop distance.
	 */
	private int limit;

	/**
	 * Maps to hold forward and backward BFS results
	 */
	private Map<GraphObject, Integer> mapBackward;
	private Map<GraphObject, Integer> mapForward;

	/**
	 * Constructor for Neighborhood Query.
	 */
	public NeighborhoodQuery(Set<Node> sourceNodes, Direction direction, int limit)
	{
		this.sourceNodes = sourceNodes;
		this.direction = direction;
		this.limit = limit;
	}

	/**
	 * Method to run query
	 */
	public Set<GraphObject> run()
	{
		// result set of neighborhood query
		Set<GraphObject> queryResult = new HashSet<GraphObject>();

		// if upstream is selected
		if (direction == Direction.UPSTREAM || direction == Direction.BOTHSTREAM)
		{
			// run BFS in upstream direction
			BFS bfsBackward = new BFS(sourceNodes, null, Direction.UPSTREAM, this.limit);

			mapBackward = bfsBackward.run();

			// add result of BFS to result Set
			queryResult.addAll(mapBackward.keySet());
		}

		// if downstream is selected
		if (direction == Direction.DOWNSTREAM || direction == Direction.BOTHSTREAM)
		{
			// run BFS in downstream direction
			BFS bfsForward = new BFS(sourceNodes, null, Direction.DOWNSTREAM, this.limit);

			mapForward = bfsForward.run();

			// add result of BFS to result Set
			queryResult.addAll(mapForward.keySet());
		}
		
		// return the result of query
		return queryResult;
	}
}