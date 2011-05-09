package org.biopax.paxtools.query;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.query.algorithm.*;
import org.biopax.paxtools.query.model.Graph;
import org.biopax.paxtools.query.model.GraphObject;
import org.biopax.paxtools.query.model.Node;
import org.biopax.paxtools.query.wrapperL3.GraphL3;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Ozgun Babur
 */
public class    QueryExecuter
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

		Set<Node> source = graph.getWrapperSet(sourceSet);

		NeighborhoodQuery query = new NeighborhoodQuery(source, direction, limit);
		Set<GraphObject> resultWrappers = query.run();
		Set<Object> result = graph.getWrappedSet(resultWrappers);

		HashSet<BioPAXElement> set = new HashSet<BioPAXElement>();
		for (Object o : result)
		{
			set.add((BioPAXElement) o);
		}
		return set;
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

		Set<Node> source = graph.getWrapperSet(sourceSet);
		Set<Node> target = graph.getWrapperSet(targetSet);

		PoIQuery query = new PoIQuery(source, target, limitType, limit, true);
		Set<GraphObject> resultWrappers = query.run();
		Set<Object> result = graph.getWrappedSet(resultWrappers);

		HashSet<BioPAXElement> set = new HashSet<BioPAXElement>();

		for (Object o : result)
		{
			set.add((BioPAXElement) o);
		}
		return set;
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

		Set<Node> source = graph.getWrapperSet(sourceSet);

		CommonStreamQuery query = new CommonStreamQuery(source, direction, limit);

		Set<Node> resultWrappers = query.run();
		Set<Object> result = graph.getWrappedSet(resultWrappers);

		HashSet<BioPAXElement> set = new HashSet<BioPAXElement>();
		for (Object o : result)
		{
			set.add((BioPAXElement) o);
		}
		return set;
	}

	public static Set<BioPAXElement> runCommonStreamWithPOI(
		Set<BioPAXElement> sourceSet,
		Model model,
		Direction direction,
		int limit)
	{
		Set<BioPAXElement> result = runCommonStream(sourceSet, model, direction, limit);
		return (direction == Direction.UPSTREAM) ?
			runPOI(result, sourceSet, model, LimitType.NORMAL, limit) :
			runPOI(sourceSet, result, model, LimitType.NORMAL, limit);
	}
}
