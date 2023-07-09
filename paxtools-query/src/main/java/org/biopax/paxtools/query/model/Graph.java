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
	 * @param obj The wrapped object
	 * @return Wrapper
	 */
	GraphObject getGraphObject(Object obj);

	/**
	 * Gets the set of wrappers for the given wrapped object set.
	 * @param objects Wrapped objects
	 * @return Related wrappers
	 */
	Set<Node> getWrapperSet(Set<?> objects);

	/**
	 * Gets a map from objects to their wrappers.
	 * @param objects Wrapped objects
	 * @return Object-to-wrapper map
	 */
	Map<Object, Node> getWrapperMap(Set<?> objects);

	/**
	 * Gets the wrapped objects of the given wrapper set.
	 * @param wrappers Wrappers
	 * @return Wrapped objects
	 */
	Set<Object> getWrappedSet(Set<? extends GraphObject> wrappers);

	/**
	 * Should clear any analysis specific labeling on the graph.
	 */
	void clear();
}
