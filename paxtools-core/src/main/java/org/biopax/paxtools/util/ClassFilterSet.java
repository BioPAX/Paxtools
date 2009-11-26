package org.biopax.paxtools.util;

import java.util.Set;

/**
 * User: root Date: Aug 30, 2006 Time: 3:05:58 PM
 */
public class ClassFilterSet<E> extends AbstractFilterSet<E>
{
// ------------------------------ FIELDS ------------------------------

	private Class<E> filterClass = null;

// --------------------------- CONSTRUCTORS ---------------------------

	/**
	 * Constructor
	 *
	 * @param baseSet     to filter
	 * @param filterClass to be used as filtering parameter
	 */
	public ClassFilterSet(Set baseSet, Class<E> filterClass)
	{
		super(baseSet);
		this.filterClass = filterClass;
	}

// -------------------------- OTHER METHODS --------------------------

	protected boolean filter(Object value)
	{
		return filterClass.isInstance(value);
	}

}
