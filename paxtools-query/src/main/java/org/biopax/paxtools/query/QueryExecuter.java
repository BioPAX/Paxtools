package org.biopax.paxtools.query;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.query.algorithm.CommonStreamQuery;
import org.biopax.paxtools.query.algorithm.NeighborhoodQuery;
import org.biopax.paxtools.query.algorithm.PoIQuery;
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
		boolean upstream,
		boolean downstream)
	{
		Graph graph;

		if (model.getLevel() == BioPAXLevel.L3)
		{
			graph = new GraphL3(model);
		}
		else return null;

		Set<Node> source = graph.getWrapperSet(sourceSet);

		NeighborhoodQuery query = new NeighborhoodQuery(source, upstream, downstream, limit);
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
		return runPOI(sourceSet, sourceSet, model, PoIQuery.NORMAL_LIMIT, limit);
	}

	public static Set<BioPAXElement> runPOI(
		Set<BioPAXElement> sourceSet,
		Set<BioPAXElement> targetSet,
		Model model,
		boolean limitType,
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
		boolean direction,
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
		boolean direction,
		int limit)
	{
		Set<BioPAXElement> result = runCommonStream(sourceSet, model, direction, limit);
		return (direction == CommonStreamQuery.UPSTREAM) ?
			runPOI(result, sourceSet, model, PoIQuery.NORMAL_LIMIT, limit) :
			runPOI(sourceSet, result, model, PoIQuery.NORMAL_LIMIT, limit);
	}
	

}
