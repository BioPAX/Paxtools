package org.biopax.paxtools.query;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.query.algorithm.*;
import org.biopax.paxtools.query.model.Graph;
import org.biopax.paxtools.query.model.GraphObject;
import org.biopax.paxtools.query.model.Node;
import org.biopax.paxtools.query.wrapperL3.GraphL3;

import java.util.*;

/**
 * @author Ozgun Babur
 */
public class QueryExecuter
{
    /**
     * @see #runNeighborhood(java.util.Set, org.biopax.paxtools.model.Model, int, org.biopax.paxtools.query.algorithm.Direction)
     */
	public static Set<BioPAXElement> runNeighborhood(
		Set<BioPAXElement> sourceSet,
		Model model,
		int limit,
		Direction direction)
	{
        return runNeighborhood(sourceSet, model, limit, direction, null);
    }
	/**
	 * Gets neighborhood of the source set.
	 *
	 * @param sourceSet seed to the query
	 * @param model BioPAX model
	 * @param limit neigborhood distance to get
	 * @param direction UPSTREAM, DOWNSTREAM or BOTHSTREAM
	 * @param ubiqueIDs RDF IDs of ubiquitous entity references. can be null
	 * @return BioPAX elements in the result set
	 */
	public static Set<BioPAXElement> runNeighborhood(
		Set<BioPAXElement> sourceSet,
		Model model,
		int limit,
		Direction direction,
		Set<String> ubiqueIDs)
	{
		Graph graph;

		if (model.getLevel() == BioPAXLevel.L3)
		{
			graph = new GraphL3(model, ubiqueIDs);
		}
		else return null;

		Set<Node> source = prepareSingleNodeSet(sourceSet, graph);

		NeighborhoodQuery query = new NeighborhoodQuery(source, direction, limit);
		Set<GraphObject> resultWrappers = query.run();
		return convertQueryResult(resultWrappers, graph);
	}

    /**
     * @see #runPathsBetween(java.util.Set, org.biopax.paxtools.model.Model, int)
     *
     */
    public static Set<BioPAXElement> runPathsBetween(Set<BioPAXElement> sourceSet, Model model, int limit)
	{
        return runPathsBetween(sourceSet, model, limit, null);
    }

	/**
	 * Gets the graph constructed by the paths between the given seed nodes. Does not get paths
	 * between physical entities that belong the same entity reference.
	 * @param sourceSet Seed to the query
	 * @param model BioPAX model
	 * @param limit Length limit for the paths to be found
	 * @param ubiqueIDs RDF IDs of ubiquitous PEs. Can be null
	 * @return BioPAX elements in the result
	 */
	public static Set<BioPAXElement> runPathsBetween(Set<BioPAXElement> sourceSet, Model model,
		int limit, Set<String> ubiqueIDs)
	{
		Graph graph;

		if (model.getLevel() == BioPAXLevel.L3)
		{
			graph = new GraphL3(model, ubiqueIDs);
		}
		else return null;

		Collection<Set<Node>> sourceWrappers = prepareNodeSets(sourceSet, graph);

		if (sourceWrappers.size() < 2) return null;

		PathsBetweenQuery query = new PathsBetweenQuery(sourceWrappers, limit);
		Set<GraphObject> resultWrappers = query.run();
		return convertQueryResult(resultWrappers, graph);
	}

    /**
     * @see #runGOI(java.util.Set, org.biopax.paxtools.model.Model, int)
	 * @deprecated Use runPathsBetween instead
     */
    public static Set<BioPAXElement> runGOI(
		Set<BioPAXElement> sourceSet,
		Model model,
		int limit)
	{
        return runGOI(sourceSet, model, limit, null);
    }

	/**
	 * Gets paths between the seed nodes.
	 * @param sourceSet Seed to the query
	 * @param model BioPAX model
	 * @param limit Length limit for the paths to be found
	 * @param ubiqueIDs RDF IDs of the ubiquitous physical entities. Can be null
	 * @return BioPAX elements in the result
	 * @deprecated Use runPathsBetween instead
	 */
	public static Set<BioPAXElement> runGOI(
		Set<BioPAXElement> sourceSet,
		Model model,
		int limit,
		Set<String> ubiqueIDs)
	{
		return runPathsFromTo(sourceSet, sourceSet, model, LimitType.NORMAL, limit, ubiqueIDs);
	}

    /**
     * @see #runPathsFromTo
     *
     */
    public static Set<BioPAXElement> runPathsFromTo(
		Set<BioPAXElement> sourceSet,
		Set<BioPAXElement> targetSet,
		Model model,
		LimitType limitType,
		int limit)
	{
        return runPathsFromTo(sourceSet, targetSet, model, limitType, limit, null);
    }
	/**
	 * Gets paths the graph composed of the paths from a source node, and ends at a target node.
	 * @param sourceSet Seeds for start points of paths
	 * @param targetSet Seeds for end points of paths
	 * @param model BioPAX model
	 * @param limitType either NORMAL or SHORTEST_PLUS_K
	 * @param limit Length limit fothe paths to be found
	 * @param ubiqueIDs RDF IDs of the ubiquitous physical entities. Can be null
	 * @return BioPAX elements in the result
	 */
	public static Set<BioPAXElement> runPathsFromTo(
		Set<BioPAXElement> sourceSet,
		Set<BioPAXElement> targetSet,
		Model model,
		LimitType limitType,
		int limit,
		Set<String> ubiqueIDs)
	{
		Graph graph;

		if (model.getLevel() == BioPAXLevel.L3)
		{
			graph = new GraphL3(model, ubiqueIDs);
		}
		else return null;

		Set<Node> source = prepareSingleNodeSet(sourceSet, graph);
		Set<Node> target = prepareSingleNodeSet(targetSet, graph);

		PathsFromToQuery query = new PathsFromToQuery(source, target, limitType, limit, true);
		Set<GraphObject> resultWrappers = query.run();
		return convertQueryResult(resultWrappers, graph);
	}

    /**
     * @see #runCommonStream(java.util.Set, org.biopax.paxtools.model.Model, org.biopax.paxtools.query.algorithm.Direction, int)
     */
    public static Set<BioPAXElement> runCommonStream(
		Set<BioPAXElement> sourceSet,
		Model model,
		Direction direction,
		int limit)
	{
        return runCommonStream(sourceSet, model, direction, limit, null);

    }
	/**
	 * Gets the elements in the common upstream or downstream of the seed
	 * @param sourceSet Seed to the query
	 * @param model BioPAX model
	 * @param direction UPSTREAM or DOWNSTREAM
	 * @param limit Length limit for the search
	 * @param ubiqueIDs RDF IDs of the ubiquitous physical entities. Can be null
	 * @return BioPAX elements in the result
	 */
	public static Set<BioPAXElement> runCommonStream(
		Set<BioPAXElement> sourceSet,
		Model model,
		Direction direction,
		int limit,
		Set<String> ubiqueIDs)
	{
		Graph graph;

		if (model.getLevel() == BioPAXLevel.L3)
		{
			graph = new GraphL3(model, ubiqueIDs);
		}
		else return null;

		Collection<Set<Node>> source = prepareNodeSets(sourceSet, graph);

		CommonStreamQuery query = new CommonStreamQuery(source, direction, limit);

		Set<GraphObject> resultWrappers = query.run();
		return convertQueryResult(resultWrappers, graph);
	}

    /**
     * @see #runCommonStreamWithPOI(java.util.Set, org.biopax.paxtools.model.Model, org.biopax.paxtools.query.algorithm.Direction, int, java.util.Set)
     *
     */
    public static Set<BioPAXElement> runCommonStreamWithPOI(
        Set<BioPAXElement> sourceSet,
        Model model,
        Direction direction,
        int limit)
    {
        return runCommonStreamWithPOI(sourceSet, model, direction, limit, null);
    }

	/**
	 * First finds the common stream, then completes it with the paths between seed and common
	 * stream.
	 * @param sourceSet Seed to the query
	 * @param model BioPAX model
	 * @param direction UPSTREAM or DOWNSTREAM
	 * @param limit Length limit for the search
	 * @param ubiqueIDs RDF IDs of the ubiquitous physical entities. Can be null
	 * @return BioPAX elements in the result
	 */
	public static Set<BioPAXElement> runCommonStreamWithPOI(
		Set<BioPAXElement> sourceSet,
		Model model,
		Direction direction,
		int limit,
		Set<String> ubiqueIDs)
	{
		Graph graph;

		if (model.getLevel() == BioPAXLevel.L3)
		{
			graph = new GraphL3(model, ubiqueIDs);
		}
		else return null;

		Collection<Set<Node>> sourceSets = prepareNodeSets(sourceSet, graph);

		// Run a common stream query

		CommonStreamQuery commStream = new CommonStreamQuery(sourceSets, direction, limit);

		Set<GraphObject> resultWrappers = commStream.run();

		// Stop if they have no common stream.
		if (resultWrappers.isEmpty()) return null;

		// Extract nodes from the result

		Set<Node> target = new HashSet<Node>();

		for (GraphObject go : resultWrappers)
		{
			if (go instanceof Node) target.add((Node) go);
		}

		// Take union of the sources

		Set<Node> source = new HashSet<Node>();
		for (Set<Node> set : sourceSets)
		{
			source.addAll(set);
		}

		// Run a paths-of-interest query between source set and result set

		PathsFromToQuery poi;

		if (direction == Direction.DOWNSTREAM)
		{
			poi = new PathsFromToQuery(source, target, LimitType.NORMAL, limit, true);
		}
		else
		{
			poi = new PathsFromToQuery(target, source, LimitType.NORMAL, limit, true);
		}

		resultWrappers = poi.run();
		return convertQueryResult(resultWrappers, graph);
	}

	/**
	 * Converts the query result from wrappers to wrapped biopax elements.
	 * @param resultWrappers
	 * @param graph
	 * @return
	 */
	private static HashSet<BioPAXElement> convertQueryResult(
		Set<GraphObject> resultWrappers, Graph graph)
	{
		Set<Object> result = graph.getWrappedSet(resultWrappers);

		HashSet<BioPAXElement> set = new HashSet<BioPAXElement>();
		for (Object o : result)
		{
			set.add((BioPAXElement) o);
		}
		return set;
	}

	/**
	 * Gets the related physical entities and wraps in a single node set.
	 * @param elements
	 * @param graph
	 * @return
	 */
	public static Set<Node> prepareSingleNodeSet(Set<BioPAXElement> elements, Graph graph)
	{
		Map<BioPAXElement, Set<PhysicalEntity>> map = getRelatedPhysicalEntityMap(elements);

		Set<PhysicalEntity> pes = new HashSet<PhysicalEntity>();
		for (Set<PhysicalEntity> valueSet : map.values())
		{
			pes.addAll(valueSet);
		}

		Set<Node> nodes = graph.getWrapperSet(pes);

		// If there are interactions in the seed add them too

		Set<Node> inters = getSeedInteractions(elements, graph);
		nodes.addAll(inters);

		return nodes;
	}

	private static Collection<Set<Node>> prepareNodeSets(Set<BioPAXElement> elements, Graph graph)
	{
		Collection<Set<Node>> sets = new HashSet<Set<Node>>();

		Map<BioPAXElement, Set<PhysicalEntity>> map = getRelatedPhysicalEntityMap(elements);

		for (Set<PhysicalEntity> pes : map.values())
		{
			Set<Node> set = graph.getWrapperSet(pes);

			if (!set.isEmpty()) sets.add(set);
		}
		
		// Add interactions in the seed as single node set

		Set<Node> inters = getSeedInteractions(elements, graph);
		for (Node node : inters)
		{
			sets.add(Collections.singleton(node));
		}

		return sets;
	}

	/**
	 * Maps each BioPAXElement to its related PhysicalEntity objects.
	 *
	 * @param elements
	 * @return
	 */
	public static Map<BioPAXElement, Set<PhysicalEntity>> getRelatedPhysicalEntityMap(
		Collection<BioPAXElement> elements)
	{
		Map<BioPAXElement, Set<PhysicalEntity>> map = new HashMap<BioPAXElement, Set<PhysicalEntity>>();

		for (BioPAXElement ele : elements)
		{
			Set<PhysicalEntity> ents = getRelatedPhysicalEntities(ele, null);

			if (!ents.isEmpty())
			{
				map.put(ele, ents);
			}
		}
		return map;
	}

	/**
	 * Gets the related PhysicalEntity objects of the given BioPAXElement, in level 3 models.
	 *
	 * @param element to get related PhysicalEntity objects
	 * @param pes result set. if not supplied, a new set will be initialized.
	 * @return
	 */
	public static Set<PhysicalEntity> getRelatedPhysicalEntities(BioPAXElement element,
		Set<PhysicalEntity> pes)
	{
		if (pes == null) pes = new HashSet<PhysicalEntity>();

		if (element instanceof Complex)
		{
			Complex cpx = (Complex) element;

			if (!pes.contains(cpx))
			{
				pes.add(cpx);
				for (Complex parent : cpx.getComponentOf())
				{
					getRelatedPhysicalEntities(parent, pes);
				}
			}
		}
		else if (element instanceof PhysicalEntity)
		{
			PhysicalEntity pe = (PhysicalEntity) element;
			if (!pes.contains(pe))
			{
				pes.add(pe);
	
				for (Complex cmp : pe.getComponentOf())
				{
					getRelatedPhysicalEntities(cmp, pes);
				}

				// This is a hack for BioPAX graph. Equivalence relations do not link members and
				// complexes because members cannot be addressed. Below call makes sure that if the
				// source node has a generic parents or children and they appear in a complex, we
				// include the complex in the sources.
				addEquivalentsComplexes(pe, pes);
			}
		}
		else if (element instanceof Xref)
		{
			for (XReferrable xrable : ((Xref) element).getXrefOf())
			{
				getRelatedPhysicalEntities(xrable, pes);
			}
		}
		else if (element instanceof EntityReference)
		{
			for (SimplePhysicalEntity spe : ((EntityReference) element).getEntityReferenceOf())
			{
				getRelatedPhysicalEntities(spe, pes);
			}
		}

		return pes;
	}

	private static void addEquivalentsComplexes(PhysicalEntity pe, Set<PhysicalEntity> pes)
	{
		addEquivalentsComplexes(pe, true, pes);
		addEquivalentsComplexes(pe, false, pes);
	}

	private static void addEquivalentsComplexes(PhysicalEntity pe, boolean outer,
		Set<PhysicalEntity> pes)
	{
		Set<PhysicalEntity> set = outer ? 
			pe.getMemberPhysicalEntityOf() : pe.getMemberPhysicalEntity();

		for (PhysicalEntity related : set)
		{
			for (Complex cmp : related.getComponentOf())
			{
				getRelatedPhysicalEntities(cmp, pes);
			}

			addEquivalentsComplexes(related, outer, pes);
		}
	}

	/**
	 * Extracts the queryable interactions from the elements.
	 *
	 * @param elements
	 * @return
	 */
	public static Set<Node> getSeedInteractions(Collection<BioPAXElement> elements, Graph graph)
	{
		Set<Node> nodes = new HashSet<Node>();

		for (BioPAXElement ele : elements)
		{
			if (ele instanceof Conversion || ele instanceof TemplateReaction ||
				ele instanceof Control)
			{
				GraphObject go = graph.getGraphObject(ele);

				if (go instanceof Node)
				{
					nodes.add((Node) go);
				}
			}
		}
		return nodes;
	}

}
