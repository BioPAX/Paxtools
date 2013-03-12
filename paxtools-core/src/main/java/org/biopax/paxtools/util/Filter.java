package org.biopax.paxtools.util;


/**
 * Generic interface for defining filter decorators. Filters are useful for defining restrictions on
 * properties, selecting subsets of objects or defining the behaviour of traversers.
 * @param <T>
 */
public interface Filter<T>
{
	/**
 	 * @param object to be filtered
	 * @return true if the object satisfies the constraint defined by this filter.
	 */
	boolean filter(T object);
}
