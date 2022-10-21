package org.biopax.paxtools.query;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.query.algorithm.*;
import org.biopax.paxtools.query.model.Graph;
import org.biopax.paxtools.query.model.GraphObject;
import org.biopax.paxtools.query.model.Node;
import org.biopax.paxtools.query.wrapperL3.Filter;
import org.biopax.paxtools.query.wrapperL3.GraphL3;
import org.biopax.paxtools.query.wrapperL3undirected.GraphL3Undirected;

import java.util.*;

/**
 * This class provides static methods to execute graph queries. These cover only the most frequent
 * use cases. Users can use these methods as example for executing the query they need.
 *
 * @author Ozgun Babur
 */
public class QueryExecuter
{
	/**
	 * Gets neighborhood of the source set.
	 *
	 * @param sourceSet seed to the query
	 * @param model BioPAX model
	 * @param limit neigborhood distance to get
	 * @param direction UPSTREAM, DOWNSTREAM or BOTHSTREAM
	 * @param filters for filtering graph elements
	 * @return BioPAX elements in the result set
	 */
	public static Collection<BioPAXElement> runNeighborhood(
		Collection<BioPAXElement> sourceSet,
		Model model,
		int limit,
		Direction direction,
		Filter... filters)
	{
		Graph graph;

		if (model.getLevel() == BioPAXLevel.L3)
		{
			if (direction == Direction.UNDIRECTED)
			{
				graph = new GraphL3Undirected(model, filters);
				direction = Direction.BOTHSTREAM;
			}
			else
			{
				graph = new GraphL3(model, filters);
			}
		}
		else return Collections.emptySet();

		Collection<Node> source = prepareSingleNodeSet(sourceSet, graph);

		if (sourceSet.isEmpty()) return Collections.emptySet();

		NeighborhoodQuery query = new NeighborhoodQuery(source, direction, limit);
		Collection<GraphObject> resultWrappers = query.run();

		return convertQueryResult(resultWrappers, graph, true);
	}

	/**
	 * Gets neighborhood of the source set.
	 *
	 * @param sourceSets seed to the query
	 * @param model BioPAX model
	 * @param limit neigborhood distance to get
	 * @param direction UPSTREAM, DOWNSTREAM or BOTHSTREAM
	 * @param filters for filtering graph elements
	 * @return BioPAX elements in the result set
	 */
	public static Collection<BioPAXElement> runNeighborhoodMultiSet(
		Collection<Collection<BioPAXElement>> sourceSets,
		Model model,
		int limit,
		Direction direction,
		Filter... filters)
	{
		Graph graph;

		if (model.getLevel() == BioPAXLevel.L3)
		{
			if (direction == Direction.UNDIRECTED)
			{
				graph = new GraphL3Undirected(model, filters);
				direction = Direction.BOTHSTREAM;
			}
			else
			{
				graph = new GraphL3(model, filters);
			}
		}
		else return Collections.emptySet();

		Collection<Node> source = prepareSingleNodeSetFromSets(sourceSets, graph);

		if (sourceSets.isEmpty()) return Collections.emptySet();

		NeighborhoodQuery query = new NeighborhoodQuery(source, direction, limit);
		Collection<GraphObject> resultWrappers = query.run();
		return convertQueryResult(resultWrappers, graph, true);
	}

	/**
	 * Gets the graph constructed by the paths between the given seed nodes. Does not get paths
	 * between physical entities that belong the same entity reference.
	 * @param sourceSet Seed to the query
	 * @param model BioPAX model
	 * @param limit Length limit for the paths to be found
	 * @param filters optional filters - for filtering graph elements
	 * @return BioPAX elements in the result
	 */
	public static Collection<BioPAXElement> runPathsBetween(Collection<BioPAXElement> sourceSet, Model model,
		int limit, Filter... filters)
	{
		Graph graph;

		if (model.getLevel() == BioPAXLevel.L3)
		{
			graph = new GraphL3(model, filters);
		}
		else return Collections.emptySet();

		Collection<Set<Node>> sourceWrappers = prepareNodeSets(sourceSet, graph);

		if (sourceWrappers.size() < 2) return Collections.emptySet();

		PathsBetweenQuery query = new PathsBetweenQuery(sourceWrappers, limit);
		Collection<GraphObject> resultWrappers = query.run();

		return convertQueryResult(resultWrappers, graph, true);
	}

	/**
	 * Gets the graph constructed by the paths between the given seed nodes. Does not get paths
	 * between physical entities that belong the same entity reference.
	 * @param sourceSets Seed to the query
	 * @param model BioPAX model
	 * @param limit Length limit for the paths to be found
	 * @param filters optional filters - for filtering graph elements
	 * @return BioPAX elements in the result
	 */
	public static Collection<BioPAXElement> runPathsBetweenMultiSet(
		Collection<Collection<BioPAXElement>> sourceSets, Model model,
		int limit, Filter... filters)
	{
		Graph graph;

		if (model.getLevel() == BioPAXLevel.L3)
		{
			graph = new GraphL3(model, filters);
		}
		else return Collections.emptySet();

		Collection<Set<Node>> sourceWrappers = prepareNodeSetsFromSets(sourceSets, graph);

		if (sourceWrappers.size() < 2) return Collections.emptySet();

		PathsBetweenQuery query = new PathsBetweenQuery(sourceWrappers, limit);
		Collection<GraphObject> resultWrappers = query.run();
		return convertQueryResult(resultWrappers, graph, true);
	}

	/**
	 * Gets paths between the seed nodes.
	 * @param sourceSet Seed to the query
	 * @param model BioPAX model
	 * @param limit Length limit for the paths to be found
	 * @param filters for filtering graph elements
	 * @return BioPAX elements in the result
	 * @deprecated Use runPathsBetween instead
	 */
	public static Collection<BioPAXElement> runGOI(
		Collection<BioPAXElement> sourceSet,
		Model model,
		int limit,
		Filter... filters)
	{
		return runPathsFromTo(sourceSet, sourceSet, model, LimitType.NORMAL, limit, filters);
	}

	/**
	 * Gets paths the graph composed of the paths from a source node, and ends at a target node.
	 * @param sourceSet Seeds for start points of paths
	 * @param targetSet Seeds for end points of paths
	 * @param model BioPAX model
	 * @param limitType either NORMAL or SHORTEST_PLUS_K
	 * @param limit Length limit fothe paths to be found
	 * @param filters for filtering graph elements
	 * @return BioPAX elements in the result
	 */
	public static Collection<BioPAXElement> runPathsFromTo(
		Collection<BioPAXElement> sourceSet,
		Collection<BioPAXElement> targetSet,
		Model model,
		LimitType limitType,
		int limit,
		Filter... filters)
	{
		Graph graph;

		if (model.getLevel() == BioPAXLevel.L3)
		{
			graph = new GraphL3(model, filters);
		}
		else return Collections.emptySet();

		Collection<Node> source = prepareSingleNodeSet(sourceSet, graph);
		Collection<Node> target = prepareSingleNodeSet(targetSet, graph);

		PathsFromToQuery query = new PathsFromToQuery(source, target, limitType, limit, true);
		Collection<GraphObject> resultWrappers = query.run();

		return convertQueryResult(resultWrappers, graph, true);
	}

	/**
	 * Gets paths the graph composed of the paths from a source node, and ends at a target node.
	 * @param sourceSets Seeds for start points of paths
	 * @param targetSets Seeds for end points of paths
	 * @param model BioPAX model
	 * @param limitType either NORMAL or SHORTEST_PLUS_K
	 * @param limit Length limit fothe paths to be found
	 * @param filters for filtering graph elements
	 * @return BioPAX elements in the result
	 */
	public static Collection<BioPAXElement> runPathsFromToMultiSet(
		Collection<Collection<BioPAXElement>> sourceSets,
		Collection<Collection<BioPAXElement>> targetSets,
		Model model,
		LimitType limitType,
		int limit,
		Filter... filters)
	{
		Graph graph;

		if (model.getLevel() == BioPAXLevel.L3)
		{
			graph = new GraphL3(model, filters);
		}
		else return Collections.emptySet();

		Collection<Node> source = prepareSingleNodeSetFromSets(sourceSets, graph);
		Collection<Node> target = prepareSingleNodeSetFromSets(targetSets, graph);

		PathsFromToQuery query = new PathsFromToQuery(source, target, limitType, limit, true);
		Collection<GraphObject> resultWrappers = query.run();

		return convertQueryResult(resultWrappers, graph, true);
	}

	/**
	 * Gets the elements in the common upstream or downstream of the seed
	 * @param sourceSet Seed to the query
	 * @param model BioPAX model
	 * @param direction UPSTREAM or DOWNSTREAM
	 * @param limit Length limit for the search
	 * @param filters for filtering graph elements
	 * @return BioPAX elements in the result
	 */
	public static Collection<BioPAXElement> runCommonStream(
		Collection<BioPAXElement> sourceSet,
		Model model,
		Direction direction,
		int limit,
		Filter... filters)
	{
		Graph graph;

		if (model.getLevel() == BioPAXLevel.L3)
		{
			graph = new GraphL3(model, filters);
		}
		else return Collections.emptySet();

		Collection<Set<Node>> source = prepareNodeSets(sourceSet, graph);

		if (sourceSet.size() < 2) return Collections.emptySet();

		CommonStreamQuery query = new CommonStreamQuery(source, direction, limit);

		Collection<GraphObject> resultWrappers = query.run();

		return convertQueryResult(resultWrappers, graph, false);
	}

	/**
	 * Gets the elements in the common upstream or downstream of the seed
	 * @param sourceSets Seed to the query
	 * @param model BioPAX model
	 * @param direction UPSTREAM or DOWNSTREAM
	 * @param limit Length limit for the search
	 * @param filters for filtering graph elements
	 * @return BioPAX elements in the result
	 */
	public static Collection<BioPAXElement> runCommonStreamMultiSet(
		Collection<Collection<BioPAXElement>> sourceSets,
		Model model,
		Direction direction,
		int limit,
		Filter... filters)
	{
		Graph graph;

		if (model.getLevel() == BioPAXLevel.L3)
		{
			graph = new GraphL3(model, filters);
		}
		else return Collections.emptySet();

		Collection<Set<Node>> source = prepareNodeSetsFromSets(sourceSets, graph);

		if (source.size() < 2) return Collections.emptySet();

		CommonStreamQuery query = new CommonStreamQuery(source, direction, limit);

		Collection<GraphObject> resultWrappers = query.run();

		return convertQueryResult(resultWrappers, graph, false);
	}

	/**
	 * First finds the common stream, then completes it with the paths between seed and common
	 * stream.
	 * @param sourceSet Seed to the query
	 * @param model BioPAX model
	 * @param direction UPSTREAM or DOWNSTREAM
	 * @param limit Length limit for the search
	 * @param filters for filtering graph elements
	 * @return BioPAX elements in the result
	 */
	public static Collection<BioPAXElement> runCommonStreamWithPOI(
		Collection<BioPAXElement> sourceSet,
		Model model,
		Direction direction,
		int limit,
		Filter... filters)
	{
		Graph graph;

		if (model.getLevel() == BioPAXLevel.L3)
		{
			graph = new GraphL3(model, filters);
		}
		else return Collections.emptySet();

		Collection<Set<Node>> sourceSets = prepareNodeSets(sourceSet, graph);

		if (sourceSet.size() < 2) return Collections.emptySet();

		return runCommonStreamWithPOIContinued(sourceSets, direction, limit, graph);
	}

	/**
	 * First finds the common stream, then completes it with the paths between seed and common
	 * stream.
	 * @param sourceSets Seed to the query
	 * @param model BioPAX model
	 * @param direction UPSTREAM or DOWNSTREAM
	 * @param limit Length limit for the search
	 * @param filters for filtering graph elements
	 * @return BioPAX elements in the result
	 */
	public static Collection<BioPAXElement> runCommonStreamWithPOIMultiSet(
		Collection<Collection<BioPAXElement>> sourceSets,
		Model model,
		Direction direction,
		int limit,
		Filter... filters)
	{
		Graph graph;

		if (model.getLevel() == BioPAXLevel.L3)
		{
			graph = new GraphL3(model, filters);
		}
		else return Collections.emptySet();

		Collection<Set<Node>> nodes = prepareNodeSetsFromSets(sourceSets, graph);

		if (nodes.size() < 2) return Collections.emptySet();

		return runCommonStreamWithPOIContinued(nodes, direction, limit, graph);
	}

	private static Collection<BioPAXElement> runCommonStreamWithPOIContinued(Collection<Set<Node>> sourceSets,
		Direction direction, int limit, Graph graph)
	{
		// Run a common stream query

		CommonStreamQuery commStream = new CommonStreamQuery(sourceSets, direction, limit);

		Collection<GraphObject> resultWrappers = commStream.run();

		// Stop if they have no common stream.
		if (resultWrappers.isEmpty()) return Collections.emptySet();

		// Extract nodes from the result

		Collection<Node> target = new HashSet<>();

		for (GraphObject go : resultWrappers)
		{
			if (go instanceof Node) target.add((Node) go);
		}

		// Take union of the sources

		Collection<Node> source = new HashSet<>();
		for (Collection<Node> set : sourceSets)
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

		return convertQueryResult(resultWrappers, graph, true);
	}

	/**
	 * Converts the query result from wrappers to wrapped BioPAX elements.
	 * @param resultWrappers Wrappers of the result set
	 * @param graph Queried graph
	 * @param removeDisconnected whether to remove disconnected non-complex type physical entities
	 * @return Set of elements in the result
	 */
	private static Collection<BioPAXElement> convertQueryResult(
		Collection<GraphObject> resultWrappers, Graph graph, boolean removeDisconnected)
	{
		Collection<Object> result = graph.getWrappedSet(resultWrappers);

		Collection<BioPAXElement> set = new HashSet<>();
		for (Object o : result)
		{
			set.add((BioPAXElement) o);
		}

		// remove disconnected simple physical entities
		if (removeDisconnected)
		{
			Collection<BioPAXElement> remove = new HashSet<>();

			for (BioPAXElement ele : set)
			{
				if (ele instanceof SimplePhysicalEntity &&
					isDisconnected((SimplePhysicalEntity) ele, set))
				{
					remove.add(ele);
				}
			}
			set.removeAll(remove);
		}
		
		return set;
	}

	private static boolean isDisconnected(SimplePhysicalEntity spe, Collection<BioPAXElement> resultSet)
	{
		for (Interaction inter : spe.getParticipantOf())
		{
			if (resultSet.contains(inter)) return false;
		}

		for (Complex complex : spe.getComponentOf())
		{
			if (resultSet.contains(complex)) return false;
		}

		for (PhysicalEntity pe : spe.getMemberPhysicalEntityOf())
		{
			if (resultSet.contains(pe)) return false;
		}

		for (PhysicalEntity pe : spe.getMemberPhysicalEntity())
		{
			if (resultSet.contains(pe)) return false;
		}

		return true;
	}

	/**
	 * Gets the related wrappers of the given elements in a set.
	 * @param elements Elements to get the related wrappers
	 * @param graph Owner graph
	 * @return Related wrappers in a set
	 */
	public static Set<Node> prepareSingleNodeSet(Collection<BioPAXElement> elements, Graph graph)
	{
		Map<BioPAXElement, Collection<PhysicalEntity>> map = getRelatedPhysicalEntityMap(elements);

		Collection<PhysicalEntity> pes = new HashSet<>();
		for (Collection<PhysicalEntity> valueSet : map.values())
		{
			pes.addAll(valueSet);
		}

		Set<Node> nodes = graph.getWrapperSet(pes);

		// If there are interactions in the seed add them too

		Set<Node> inters = getSeedInteractions(elements, graph);
		nodes.addAll(inters);

		return nodes;
	}

	/**
	 * Gets the related wrappers of the given elements in the sets.
	 * @param sets Sets of elements to get the related wrappers
	 * @param graph Owner graph
	 * @return Related wrappers in a set
	 */
	public static Collection<Node> prepareSingleNodeSetFromSets(Collection<Collection<BioPAXElement>> sets, Graph graph)
	{
		Collection<BioPAXElement> elements = new HashSet<>();
		for (Collection<BioPAXElement> set : sets)
		{
			elements.addAll(set);
		}
		return prepareSingleNodeSet(elements, graph);
	}

	/**
	 * Gets the related wrappers of the given elements in individual sets. An object can be related
	 * to more than one wrapper and they will appear in the same set. This method created a set for
	 * each parameter element that has a related wrapper.
	 * @param elements Elements to get the related wrappers
	 * @param graph Owner graph
	 * @return Related wrappers in individual sets
	 */
	private static Collection<Set<Node>> prepareNodeSets(Collection<BioPAXElement> elements, Graph graph)
	{
		Collection<Set<Node>> sets = new HashSet<>();

		Map<BioPAXElement, Collection<PhysicalEntity>> map = getRelatedPhysicalEntityMap(elements);

		for (Collection<PhysicalEntity> pes : map.values())
		{
			Set<Node> set = graph.getWrapperSet(pes);
			if (!set.isEmpty()) sets.add(set);
		}
		
		// Add interactions in the seed as single node set

		Collection<Node> inters = getSeedInteractions(elements, graph);
		for (Node node : inters)
		{
			sets.add(Collections.singleton(node));
		}

		return sets;
	}

	/**
	 * Gets the related wrappers of the given elements in individual sets. An object can be related
	 * to more than one wrapper and they will appear in the same set. This method created a set for
	 * each parameter element that has a related wrapper.
	 * @param sets Sets of elements to get the related wrappers
	 * @param graph Owner graph
	 * @return Related wrappers in individual sets
	 */
	private static Collection<Set<Node>> prepareNodeSetsFromSets(Collection<Collection<BioPAXElement>> sets, Graph graph)
	{
		Collection<Set<Node>> result = new HashSet<>();

		for (Collection<BioPAXElement> set : sets)
		{
			Set<Node> nodes = prepareSingleNodeSet(set, graph);
			if (!nodes.isEmpty()) result.add(nodes);
		}

		return result;
	}

	/**
	 * Maps each BioPAXElement to its related PhysicalEntity objects.
	 *
	 * @param elements Elements to map
	 * @return The mapping
	 */
	public static Map<BioPAXElement, Collection<PhysicalEntity>> getRelatedPhysicalEntityMap(
		Collection<BioPAXElement> elements)
	{
		replaceXrefsWithRelatedER(elements);
		Map<BioPAXElement, Collection<PhysicalEntity>> map = new HashMap<>();

		for (BioPAXElement ele : elements)
		{
			Collection<PhysicalEntity> ents = getRelatedPhysicalEntities(ele, null);
			if (!ents.isEmpty())
			{
				map.put(ele, ents);
			}
		}
		return map;
	}

	/**
	 * Replaces Xref objects with the related EntityReference objects. This is required for the use
	 * case when user provides multiple xrefs that point to the same ER.
	 * @param elements elements to send to a query as source or target
	 */
	protected static void replaceXrefsWithRelatedER(
		Collection<BioPAXElement> elements)
	{
		Collection<EntityReference> ers = new HashSet<>();
		Collection<Xref> xrefs = new HashSet<>();
		for (BioPAXElement element : elements)
		{
			if (element instanceof Xref)
			{
				xrefs.add((Xref) element);
				for (XReferrable able : ((Xref) element).getXrefOf())
				{
					if (able instanceof EntityReference)
					{
						ers.add((EntityReference) able);
					}
				}
			}
		}

		elements.removeAll(xrefs);
		for (EntityReference er : ers)
		{
			if (!elements.contains(er)) elements.add(er);
		}
	}

	/**
	 * Gets the related PhysicalEntity objects of the given BioPAXElement, in level 3 models.
	 *
	 * @param element Element to get related PhysicalEntity objects
	 * @param pes Result set. If not supplied, a new set will be initialized.
	 * @return Related PhysicalEntity objects
	 */
	public static Collection<PhysicalEntity> getRelatedPhysicalEntities(BioPAXElement element,
		Collection<PhysicalEntity> pes)
	{
		if (pes == null) pes = new HashSet<>();

		if (element instanceof PhysicalEntity)
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
			EntityReference er = (EntityReference) element;

			for (SimplePhysicalEntity spe : er.getEntityReferenceOf())
			{
				getRelatedPhysicalEntities(spe, pes);
			}

			for (EntityReference parentER : er.getMemberEntityReferenceOf())
			{
				getRelatedPhysicalEntities(parentER, pes);
			}
		}

		return pes;
	}

	/**
	 * Adds equivalents and parent complexes of the given PhysicalEntity to the parameter set.
	 * @param pe The PhysicalEntity to add its equivalents and complexes
	 * @param pes Set to collect equivalents and complexes
	 */
	private static void addEquivalentsComplexes(PhysicalEntity pe, Collection<PhysicalEntity> pes)
	{
		addEquivalentsComplexes(pe, true, pes);

		// Do not traverse to more specific. This was causing a bug so I commented out. Did not delete it just in case
		// later we realize that this is needed for another use case.
//		addEquivalentsComplexes(pe, false, pes);
	}

	/**
	 * Adds equivalents and parent complexes of the given PhysicalEntity to the parameter set. This
	 * method traverses homologies only to one direction (either towards parents or to the
	 * children).
	 * @param pe The PhysicalEntity to add its equivalents and complexes
	 * @param outer Give true if towards parents, false if to the children
	 * @param pes Set to collect equivalents and complexes
	 */
	private static void addEquivalentsComplexes(PhysicalEntity pe, boolean outer,
		Collection<PhysicalEntity> pes)
	{
		Collection<PhysicalEntity> set = outer ?
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
	 * Extracts the querible interactions from the elements.
	 *
	 * @param elements BioPAX elements to search
	 * @param graph graph model
	 * @return Querible Interactions (nodes)
	 */
	public static Set<Node> getSeedInteractions(Collection<BioPAXElement> elements, Graph graph)
	{
		Set<Node> nodes = new HashSet<>();

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
