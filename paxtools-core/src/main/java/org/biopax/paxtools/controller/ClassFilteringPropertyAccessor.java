package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.util.ClassFilterSet;

import java.util.Set;

/**

 */
public class ClassFilteringPropertyAccessor<D extends BioPAXElement, R,
		F extends R > extends FilteringPropertyAccessor<D,R>
{
	private Class<F> filter;

	public ClassFilteringPropertyAccessor(PropertyAccessor<D, R> impl, Class<F> filter)
	{
		super(impl);
		this.filter=filter;
	}

	@Override protected Set<? extends R> filter(Set<? extends R> valueFromBean)
	{
		return new ClassFilterSet<R,F>(valueFromBean, filter);
	}

}
