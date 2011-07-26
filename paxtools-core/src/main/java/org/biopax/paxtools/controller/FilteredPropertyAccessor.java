package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.util.ClassFilterSet;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.util.Set;

/**

 */
public class FilteredPropertyAccessor<D extends BioPAXElement, R, F extends R> extends DecoratingPropertyAccessor<D,R>
{
	private Class<F> filter;

	private FilteredPropertyAccessor(PropertyAccessor<D, R> impl, Class<F> filter)
	{
		super(impl);
		this.filter=filter;
	}

	@Override public Set<? extends R> getValueFromBean(D bean) throws IllegalBioPAXArgumentException
	{
		return new ClassFilterSet(impl.getValueFromBean(bean), filter);
	}

	public static <D extends BioPAXElement, R, F extends R> PropertyAccessor<D,R> create(PropertyAccessor<D,R> pa,
	                                                                                     Class<F> filter)
	{
		 return new FilteredPropertyAccessor<D, R, F>(pa, filter);
	}
}
