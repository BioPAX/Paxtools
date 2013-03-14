package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.util.ClassFilterSet;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.util.Set;

/**
 * This class is a decorating property accessor that filters values with a given class. It is used, for example, to
 * implement restrictions for pattern editors.
 */
public class FilteredPropertyAccessor<D extends BioPAXElement, R> extends DecoratingPropertyAccessor<D,R>
{
	private Class filter;

	private FilteredPropertyAccessor(PropertyAccessor<D, R> impl, Class filter)
	{
		super(impl);
		this.filter=filter;
	}

	@Override public Set<? extends R> getValueFromBean(D bean) throws IllegalBioPAXArgumentException
	{
		return new ClassFilterSet(impl.getValueFromBean(bean), filter);
	}

	/**
	 * FactoryMethod that creates a filtered property accessor by decorating a given accessor with a class filter.
	 * @param pa to be decorated
	 * @param filter Class to be filtered, must extend from R.
	 * @param <D> Domain of the original accessor
	 * @param <R> Range of the original accessor
	 * @return A filtered accessor.
	 */
	public static <D extends BioPAXElement, R> PropertyAccessor<D,R> create(PropertyAccessor<D,R> pa,
	                                                                                     Class filter)
	{
		 return new FilteredPropertyAccessor<D, R>(pa, filter);
	}
}
