package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.util.Set;

/**
 */
public abstract class FilteringProperyAccessor<D extends BioPAXElement, R> implements PropertyAccessor<D, R>
{

	private PropertyAccessor<D, R> impl;

	protected FilteringProperyAccessor(PropertyAccessor<D,R> impl)
	{
		this.impl = impl;
	}

	@Override public Class<D> getDomain()
	{
		return impl.getDomain();
	}

	@Override public Class<R> getRange()
	{
		return impl.getRange();
	}

	@Override public boolean isMultipleCardinality()
	{
		return isMultipleCardinality();
	}

	@Override public Set<R> getValueFromBean(D bean) throws IllegalBioPAXArgumentException
	{
		return filter(impl.getValueFromBean(bean));
	}

	protected abstract Set<R> filter(Set<R> valueFromBean);


	@Override public boolean isUnknown(Object value)
	{
		return impl.isUnknown(value);
	}
}
