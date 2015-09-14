package org.biopax.paxtools.util;


/**
 * Generic interface for defining filter decorators. Filters are useful for defining restrictions on
 * properties, selecting subsets of objects or defining the behaviour of traversers.
 */
public interface Filter<T>
{
	/**
	 * Can e.g., analyze, modify or convert the given object (or model) to another format.
	 *
 	 * @param object to be filtered
	 * @return true/false, depending on whether the object satisfied a criteria defined by this Filter;
	 * 			or - conversion/analysis was successful/unsuccessful.
	 */
	boolean filter(T object);
}
