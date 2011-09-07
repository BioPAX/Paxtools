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
	GraphObject getGraphObject(Object obj);

	Set<Node> getWrapperSet(Set<?> objects);

	Map<Object, Node> getWrapperMap(Set<?> objects);

	Set<Object> getWrappedSet(Set<? extends GraphObject> wrappers);
}
