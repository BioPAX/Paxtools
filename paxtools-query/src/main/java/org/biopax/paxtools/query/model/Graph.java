package org.biopax.paxtools.query.model;

import java.util.Set;

/**
 * This graph interface is used in graph algorithms.
 *
 * @author Ozgun Babur
 */
public interface Graph
{
	public GraphObject getGraphObject(Object obj);

	public Set<Node> getWrapperSet(Set<? extends Object> objects);

	public Set<Object> getWrappedSet(Set<? extends GraphObject> wrappers);
}
