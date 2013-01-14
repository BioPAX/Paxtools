package org.biopax.paxtools.query.model;

import java.util.Map;
import java.util.Set;

/**
 * This graph interface is used in graph algorithms.
 *
 * @author Ozgun Babur
 */
public interface Graph
{
	/**
	 * Gets the wrapper of the related object.
	 * @param obj
	 * @return
	 */
	GraphObject getGraphObject(Object obj);

	Set<Node> getWrapperSet(Set<?> objects);

	/**
	 * Gets a map from objects to their wrappers.
	 * @param objects
	 * @return
	 */
	Map<Object, Node> getWrapperMap(Set<?> objects);

	Set<Object> getWrappedSet(Set<? extends GraphObject> wrappers);

	/**
	 * Should clear any analysis specific labeling on the graph.
	 */
	void clear();
}
