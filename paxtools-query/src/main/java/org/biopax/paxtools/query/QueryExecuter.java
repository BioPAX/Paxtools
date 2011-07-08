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
	public static Set<BioPAXElement> runNeighborhood(
		Set<BioPAXElement> sourceSet,
		Model model,
		int limit,
		Direction direction)
	{
		Graph graph;

		if (model.getLevel() == BioPAXLevel.L3)
		{
			graph = new GraphL3(model);
		}
		else return null;

		Set<Node> source = prepareSingleNodeSet(sourceSet, graph);

		NeighborhoodQuery query = new NeighborhoodQuery(source, direction, limit);
		Set<GraphObject> resultWrappers = query.run();
		return convertQueryResult(resultWrappers, graph);
	}

	public static Set<BioPAXElement> runPathsBetween(Set<BioPAXElement> sourceSet, Model model,
		int limit)
	{
		Graph graph;

		if (model.getLevel() == BioPAXLevel.L3)
		{
			graph = new GraphL3(model);
		}
		else return null;

		Collection<Set<Node>> sourceWrappers = prepareNodeSets(sourceSet, graph);

		if (sourceWrappers.size() < 2) return null;

		PathsBetweenQuery query = new PathsBetweenQuery(sourceWrappers, limit);
		Set<GraphObject> resultWrappers = query.run();
		return convertQueryResult(resultWrappers, graph);
	}

	public static Set<BioPAXElement> runGOI(
		Set<BioPAXElement> sourceSet,
		Model model,
		int limit)
	{
		return runPOI(sourceSet, sourceSet, model, LimitType.NORMAL, limit);
	}

	public static Set<BioPAXElement> runPOI(
		Set<BioPAXElement> sourceSet,
		Set<BioPAXElement> targetSet,
		Model model,
		LimitType limitType,
		int limit)
	{
		Graph graph;

		if (model.getLevel() == BioPAXLevel.L3)
		{
			graph = new GraphL3(model);
		}
		else return null;

		Set<Node> source = prepareSingleNodeSet(sourceSet, graph);
		Set<Node> target = prepareSingleNodeSet(targetSet, graph);

		PoIQuery query = new PoIQuery(source, target, limitType, limit, true);
		Set<GraphObject> resultWrappers = query.run();
		return convertQueryResult(resultWrappers, graph);
	}

	public static Set<BioPAXElement> runCommonStream(
		Set<BioPAXElement> sourceSet,
		Model model,
		Direction direction,
		int limit)
	{
		Graph graph;

		if (model.getLevel() == BioPAXLevel.L3)
		{
			graph = new GraphL3(model);
		}
		else return null;

		Collection<Set<Node>> source = prepareNodeSets(sourceSet, graph);

		CommonStreamQuery query = new CommonStreamQuery(source, direction, limit);

		Set<GraphObject> resultWrappers = query.run();
		return convertQueryResult(resultWrappers, graph);
	}

	public static Set<BioPAXElement> runCommonStreamWithPOI(
		Set<BioPAXElement> sourceSet,
		Model model,
		Direction direction,
		int limit)
	{
		Graph graph;

		if (model.getLevel() == BioPAXLevel.L3)
		{
			graph = new GraphL3(model);
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

		PoIQuery poi;

		if (direction == Direction.DOWNSTREAM)
		{
			poi = new PoIQuery(source, target, LimitType.NORMAL, limit, true);
		}
		else
		{
			poi = new PoIQuery(target, source, LimitType.NORMAL, limit, true);
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
	private static Set<Node> prepareSingleNodeSet(Set<BioPAXElement> elements, Graph graph)
	{
		Map<BioPAXElement, Set<PhysicalEntity>> map = getRelatedPhysicalEntityMap(elements);

		Set<PhysicalEntity> pes = new HashSet<PhysicalEntity>();
		for (Set<PhysicalEntity> valueSet : map.values())
		{
			pes.addAll(valueSet);
		}

		return graph.getWrapperSet(pes);
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
		Map<BioPAXElement, Set<PhysicalEntity>> map =
			new HashMap<BioPAXElement, Set<PhysicalEntity>>();

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
			pes.add((PhysicalEntity) element);

			for (Complex cmp : ((PhysicalEntity) element).getComponentOf())
			{
				getRelatedPhysicalEntities(cmp, pes);
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
}
