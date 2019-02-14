package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.util.Collections;
import java.util.Set;

/**
 * This class is a decorating property accessor that filters the seed arguments, only accepts if they are assignable to
 * filter class.
 */
public class FilteredByDomainPropertyAccessor<D extends BioPAXElement, R> extends DecoratingPropertyAccessor<D,R>
{
	private Class filter;

	private FilteredByDomainPropertyAccessor(PropertyAccessor<D, R> impl, Class filter)
	{
		super(impl);
		this.filter=filter;
	}

	@Override public Set<? extends R> getValueFromBean(D bean) throws IllegalBioPAXArgumentException
	{
		if (filter.isInstance(bean))
		{
			return impl.getValueFromBean(bean);
		}
		else
		{
			return Collections.emptySet();
		}
	}

	/**
	 * FactoryMethod that creates a filtered-by-domain property accessor by decorating a given accessor with a class
	 * filter.
	 * @param pa to be decorated
	 * @param filter Class to be filtered, must extend from D.
	 * @param <D> Domain of the original accessor
	 * @param <R> Range of the original accessor
	 * @return A filtered accessor.
	 */
	public static <D extends BioPAXElement, R> PropertyAccessor<D,R> create(PropertyAccessor<D,R> pa, Class filter)
	{
		 return new FilteredByDomainPropertyAccessor<D, R>(pa, filter);
	}
}
