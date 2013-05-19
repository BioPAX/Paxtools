package org.biopax.paxtools.util;

import java.util.Set;


/**
 * A FilterSet that filters based on the class. For example, it is useful for creating a Set of Proteins from a
 * Set of BioPAXElements.
 * @param <E> Superset's type e.g. BioPAXElement
 * @param <F> Subset's type e.g. Protein. F must extend E
 */
public class ClassFilterSet<E, F extends E> extends AbstractFilterSet<E, F>
{
// ------------------------------ FIELDS ------------------------------
protected Class<F> filterClass;

	// --------------------------- CONSTRUCTORS ---------------------------

	public ClassFilterSet(Set<? extends E> baseSet,Class<F> filterClass)
	{
		super(baseSet);
		this.filterClass = filterClass;

	}

	/**
	 * This filter method implementation filters based on isInstance function.
	 * @param value Object to be tested
	 * @return true if value is instance of F.
	 */
	@Override public boolean filter(E value)
	{
		return filterClass.isInstance(value);
	}

}
