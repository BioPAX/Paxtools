package org.biopax.paxtools.query;

import org.biopax.paxtools.controller.AbstractTraverser;
import org.biopax.paxtools.controller.PropertyEditor;
import org.biopax.paxtools.controller.SimpleEditorMap;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;
import org.biopax.paxtools.model.Model;
import org.biopax.paxtools.model.level2.Level2Element;
import org.biopax.paxtools.query.algorithm.CommonStreamQuery;
import org.biopax.paxtools.query.algorithm.NeighborhoodQuery;
import org.biopax.paxtools.query.algorithm.PoIQuery;
import org.biopax.paxtools.query.model.Graph;
import org.biopax.paxtools.query.model.GraphObject;
import org.biopax.paxtools.query.model.Node;
import org.biopax.paxtools.query.wrapperL3.GraphL3;
import org.biopax.paxtools.util.ClassFilterSet;

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
	
	
	/**
	 * Finds a subset of "root" BioPAX objects of specific class (incl. sub-classes;
	 * also works with a mix of different level biopax elements.)
	 * 
	 * Note: however, such "root" elements may or may not be, a property of other
	 * elements, not included in the query (source) set.
	 * 
	 * @author rodche
	 * @param sourceSet - model elements that can be "children" (property value) of each-other
	 * @param filterClass 
	 * @return
	 */
	public static <T extends BioPAXElement> Set<T> getRootElements(Set<BioPAXElement> sourceSet, final Class<T> filterClass) {
		// copy all such elements (initially, we think all are roots...)
		final Set<T> result = new HashSet<T>();
		result.addAll(new ClassFilterSet<T>(sourceSet, filterClass));
		
		for(BioPAXElement e : sourceSet) {
			// define a special 'visitor'
			BioPAXLevel level = (e instanceof Level2Element) ? BioPAXLevel.L2 : BioPAXLevel.L3;
			AbstractTraverser traverser = new AbstractTraverser(new SimpleEditorMap(level)) 
			{
				@Override
				protected void visit(Object value, BioPAXElement parent, 
						Model model, PropertyEditor editor) {
					if(filterClass.isInstance(value)) 
						result.remove(value); 
				}
			};
			// check all biopax properties
			traverser.traverse(e, null);
		}
		
		return result;
	}

}
