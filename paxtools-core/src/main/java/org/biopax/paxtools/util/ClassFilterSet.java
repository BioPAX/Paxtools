package org.biopax.paxtools.util;

import java.util.Set;

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

	public boolean filter(E value)
	{
		return filterClass.isInstance(value);
	}

}
