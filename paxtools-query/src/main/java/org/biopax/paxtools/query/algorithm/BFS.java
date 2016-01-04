package org.biopax.paxtools.query.algorithm;

import org.biopax.paxtools.query.model.Edge;
import org.biopax.paxtools.query.model.GraphObject;
import org.biopax.paxtools.query.model.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * Implements breadth-first search. Takes a set of source nodes, distance limit and labels nodes
 * towards one direction, with their breadth distances.
 *
 * @author Ozgun Babur
 * @author Merve Cakir
 */
public class BFS
{
	private static Logger LOG = LoggerFactory.getLogger(BFS.class);
	
	/**
	 * Distance labels. Missing label interpreted as infinitive.
	 */
	protected Map<GraphObject, Integer> dist;

	/**
	 * Color labels. Missing color interpreted as white.
	 */
	protected Map<GraphObject, Integer> colors;

	/**
	 * BFS starts from source nodes. They get the label 0.
	 */
	protected Set<Node> sourceSet;

	/**
	 * BFS will not further traverse neighbors of any node in the stopSet.
	 */
	protected Set<Node> stopSet;

	/**
	 * Whether the direction is FORWARD, it is REVERSE otherwise.
	 */
	protected Direction direction;

	/**
	 * Stop distance.
	 */
	protected int limit;

	/**
	 * BFS queue.
	 */
	protected LinkedList<Node> queue;

	/**
	 * Constructor with all parameters.
	 * @param sourceSet Seed of BFS
	 * @param stopSet Nodes that won't be traversed
	 * @param direction Direction of the traversal
	 * @param limit Distance limit
	 */
	public BFS(Set<Node> sourceSet, Set<Node> stopSet, Direction direction, int limit)
	{
		if (direction != Direction.UPSTREAM && direction != Direction.DOWNSTREAM)
			throw new IllegalArgumentException("Direction has to be either upstream or downstream");

		this.sourceSet = sourceSet;
		this.stopSet = stopSet;
		this.direction = direction;
		this.limit = limit;
	}

	/**
	 * Empty constructor for other possible uses.
	 */
	public BFS()
	{
	}

	/**
	 * Executes the algorithm.
	 * @return BFS tree
	 */
	public Map<GraphObject, Integer> run()
	{
		initMaps();

		// Add all source nodes to the queue if traversal is needed

		if (limit > 0)
		{
			queue.addAll(sourceSet);
		}

		// Initialize dist and color of source set

		for (Node source : sourceSet)
		{
			setLabel(source, 0);
			setColor(source, GRAY);

			labelEquivRecursive(source, UPWARD, 0, true, false);
			labelEquivRecursive(source, DOWNWARD, 0, true, false);
		}

		// Process the queue

		while (!queue.isEmpty())
		{
			Node current = queue.remove(0);

			processNode(current);

			// Current node is processed
			setColor(current, BLACK);
		}

		return dist;
	}

	/**
	 * Initializes maps used during query.
	 */
	protected void initMaps()
	{
		// Initialize label, maps and queue

		dist = new HashMap<GraphObject, Integer>();
		colors = new HashMap<GraphObject, Integer>();
		queue = new LinkedList<Node>();
	}

	/**
	 * Processes a node.
	 * @param current The current node
	 */
	protected void processNode(Node current)
	{
		// Do not process the node if it is ubique

		if (current.isUbique())
		{
			setColor(current, BLACK);
			return;
		}

//		System.out.println("processing = " + current);

		// Process edges towards the direction

		for (Edge edge : direction == Direction.DOWNSTREAM ?
			current.getDownstream() : current.getUpstream())
		{
			assert edge != null;

			// Label the edge considering direction of traversal and type of current node

			if (direction == Direction.DOWNSTREAM || !current.isBreadthNode())
			{
				setLabel(edge, getLabel(current));
			}
			else
			{
				setLabel(edge, getLabel(current) + 1);
			}

			// Get the other end of the edge
			Node neigh = direction == Direction.DOWNSTREAM ?
				edge.getTargetNode() : edge.getSourceNode();

			assert neigh != null;

			// Decide neighbor label according to the search direction and node type
			int dist = getLabel(edge);
			if (neigh.isBreadthNode() && direction == Direction.DOWNSTREAM) dist++;

			// Check if we need to stop traversing the neighbor, enqueue otherwise
			boolean further = (stopSet == null || !isEquivalentInTheSet(neigh, stopSet)) &&
				(!neigh.isBreadthNode() || dist < limit) && !neigh.isUbique();

			// Process the neighbor if not processed or not in queue

			if (getColor(neigh) == WHITE)
			{
				// Label the neighbor
				setLabel(neigh, dist);

				if (further)
				{
					setColor(neigh, GRAY);

					// Enqueue the node according to its type

					if (neigh.isBreadthNode())
					{
						queue.addLast(neigh);
					}
					else
					{
						// Non-breadth nodes are added in front of the queue
						queue.addFirst(neigh);
					}
				}
				else
				{
					// If we do not want to traverse this neighbor, we paint it black
					setColor(neigh, BLACK);
				}
			}


			labelEquivRecursive(neigh, UPWARD, getLabel(neigh), further, !neigh.isBreadthNode());
			labelEquivRecursive(neigh, DOWNWARD, getLabel(neigh), further, !neigh.isBreadthNode());
		}
	}

	/**
	 * Labels equivalent nodes recursively.
	 * @param node Node to label equivalents
	 * @param up Traversing direction. Up means towards parents, if false then towards children
	 * @param dist The label
	 * @param enqueue Whether to enqueue equivalents
	 * @param head Where to enqueue. Head or tail.
	 */
	protected void labelEquivRecursive(Node node, boolean up, int dist,
		boolean enqueue, boolean head)
	{
		if(node == null) {
			LOG.error("labelEquivRecursive: null (Node)");
			return;
		}
		
		for (Node equiv : up ? node.getUpperEquivalent() : node.getLowerEquivalent())
		{
			if (getColor(equiv) == WHITE)
			{
				setLabel(equiv, dist);

				if (enqueue)
				{
					setColor(equiv, GRAY);

					if (head) queue.addFirst(equiv);
					else queue.add(equiv);
				}
				else
				{
					setColor(equiv, BLACK);
				}
			}

			labelEquivRecursive(equiv, up, dist, enqueue, head);
		}
	}

	/**
	 * Checks if an equivalent of the given node is in the set.
	 * @param node Node to check equivalents
	 * @param set Node set
	 * @return true if an equivalent is in the set
	 */
	protected boolean isEquivalentInTheSet(Node node, Set<Node> set)
	{
		return set.contains(node) ||
			isEquivalentInTheSet(node, UPWARD, set) || isEquivalentInTheSet(node, DOWNWARD, set); 
	}

	/**
	 * Checks if an equivalent of the given node is in the set.
	 * @param node Node to check equivalents
	 * @param direction Direction to go to get equivalents
	 * @param set Node set
	 * @return true if an equivalent is in the set
	 */
	protected boolean isEquivalentInTheSet(Node node, boolean direction, Set<Node> set)
	{
		for (Node eq : direction == UPWARD ? node.getUpperEquivalent() : node.getLowerEquivalent())
		{
			if (set.contains(eq)) return true;
			boolean isIn = isEquivalentInTheSet(eq, direction, set);
			if (isIn) return true;
		}
		return false;
	}

	/**
	 * Gets color tag of the node
	 * @param node Node to get color tag
	 * @return color tag
	 */
	protected int getColor(Node node)
	{
		if (!colors.containsKey(node))
		{
			// Absence of color is interpreted as white
			return WHITE;
		}
		else
		{
			return colors.get(node);
		}
	}

	/**
	 * Sets color tag
	 * @param node node to set color tag
	 * @param color the color tag
	 */
	protected void setColor(Node node, int color)
	{
		colors.put(node, color);
	}

	/**
	 * Gets the distance label of the object.
	 * @param go object to get the distance
	 * @return the distance label
	 */
	public int getLabel(GraphObject go)
	{
		if (!dist.containsKey(go))
		{
			// Absence of label is interpreted as infinite
			return Integer.MAX_VALUE-(limit*2);
		}
		else
		{
			return dist.get(go);
		}
	}

	/**
	 * Sets the distance label.
	 * @param go object to set the distance label
	 * @param label the distance label
	 */
	protected void setLabel(GraphObject go, int label)
	{
//		System.out.println("Labeling(" + label + "): " + go);
		dist.put(go, label);
	}

	/**
	 * Forward traversal direction.
	 */
	public static final boolean FORWARD = true;

	/**
	 * Backward traversal direction.
	 */
	public static final boolean BACKWARD = false;

	/**
	 * Forward traversal direction.
	 */
	public static final boolean UPWARD = true;

	/**
	 * Backward traversal direction.
	 */
	public static final boolean DOWNWARD = false;

	/**
	 * Color white indicates the node is not processed.
	 */
	public static final int WHITE = 0;

	/**
	 * Color gray indicates that the node is in queue waiting to be procecessed.
	 */
	public static final int GRAY = 1;

	/**
	 * Color black indicates that the node was processed.
	 */
	public static final int BLACK = 2;
}
