package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.util.Set;

/**
 */
public abstract class DecoratingPropertyAccessor<D extends BioPAXElement, R> implements PropertyAccessor<D, R>
{

	protected PropertyAccessor<D, R> impl;

	protected DecoratingPropertyAccessor(PropertyAccessor<D, R> impl)
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
		return impl.isMultipleCardinality();
	}


	@Override public boolean isUnknown(Object value)
	{
		return impl.isUnknown(value);
	}
}
