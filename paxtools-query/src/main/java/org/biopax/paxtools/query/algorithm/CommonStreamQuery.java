package org.biopax.paxtools.query.algorithm;

import org.biopax.paxtools.query.model.GraphObject;
import org.biopax.paxtools.query.model.Node;

import java.util.*;

/**
 * Searches common downstream or common upstream of a specified set of entities
 * based on the given direction within the boundaries of a specified length
 * limit. Takes a source set of entities, direction of the query and
 * distance limit.
 *
 * @author Ozgun Babur
 * @author Merve Cakir
 * @author Shatlyk Ashyralyev
 */
public class CommonStreamQuery
{
	/**
	 * Collection of Set of nodes.
	 * Each Set contains all states of corresponding physical entity or
	 * contains one of the selected nodes
	 */
	private Collection<Set<Node>> sourceSet;
	
	/**
	 * The direction to determine whether the search will be to look for common
	 * downstream or common upstream.
	 */
	private Direction direction;

	/**
	 * Stop distance.
	 */
	private int limit;

	/**
	 * The map to hold the reached counts of graph objects. Reached count
	 * represents whether the particular graph object is in the boundaries
	 * of BFS.
	 */
	Map<GraphObject, Integer> reachedCount = new HashMap<GraphObject, Integer>();

	/**
	 * Constructor for Common Stream with Selected Nodes.
	 */
	public CommonStreamQuery(Set<Node> sourceNodeSet, Direction direction, int limit)
	{
		assert direction != Direction.BOTHSTREAM :
			"BOTHSTREAM is not a valid parameter for CommonStream";

		this.sourceSet = new LinkedHashSet<Set<Node>>();
		
		//Each set contains only one selected Node
		for (Node node : sourceNodeSet)
		{
			Set<Node> sourceNode = new HashSet<Node>();
			sourceNode.add(node);
			sourceSet.add(sourceNode);
		}
		
		this.direction = direction;
		this.limit = limit;
	}  
	
	/**
	 * Constructor for Common Stream with Entity States.
	 */
	public CommonStreamQuery(Collection<Set<Node>> sourceStateSet, Direction direction, int limit)
	{
		assert direction != Direction.BOTHSTREAM :
			"BOTHSTREAM is not a valid parameter for CommonStream";

		this.sourceSet = sourceStateSet;
		this.direction = direction;
		this.limit = limit;
	}

	/**
	 * Method to run query
	 */
	public Set<GraphObject> run()
	{
		/**
		 * Candidate contains all the graph objects that are the results of BFS.
		 * Eliminating nodes from candidate according to the reached counts
		 * will yield result.
		 */
		Map<GraphObject, Integer> candidate = new HashMap<GraphObject, Integer>();
		Set<GraphObject> result = new HashSet<GraphObject>();
		
   		//for each set of states of entity, run BFS separately
		for (Set<Node> source : sourceSet)
		{
			//run BFS for set of states of each entity
		  	BFS bfs = new BFS (source, null, direction, limit);
			Map<GraphObject, Integer> BFSResult = new HashMap<GraphObject, Integer>();
			BFSResult.putAll(bfs.run());

			/**
			 * Reached counts of the graph objects that are in BFSResult will
			 * be incremented by 1.
			 */
			for (GraphObject go : BFSResult.keySet())
			{
				setLabel(go, (getLabel(go) + 1));
			}
			
			//put BFS Result into candidate set
			candidate.putAll(BFSResult);
		}
			
		/**
		 * Having a reached count equal to number of nodes in the source set
		 * indicates being in common stream. 
		 */
		for(GraphObject go : candidate.keySet())
		{
			if (getLabel(go) == sourceSet.size())
			{
				result.add(go);
			}
		}

		//Return the result of query
		return result;
	}
	
	/**
	 * Method for getting Label of GraphObject.
	 * If Label is absent, then it returns 0.
	 */
	private int getLabel(GraphObject go)
	{
		if (!reachedCount.containsKey(go))
		{
			// Absence of label is interpreted as zero
			return 0;
		}
		else
		{
			return reachedCount.get(go);
		}
	}

	/**
	 * Method for setting the Label of GraphObject
	 */
	private void setLabel(GraphObject go, int label)
	{
		reachedCount.put(go, label);
	}

}
