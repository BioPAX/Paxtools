package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;

/**
 */
public abstract class DecoratingPropertyAccessor<D extends BioPAXElement, R> extends  PropertyAccessorAdapter<D, R>
{

	protected PropertyAccessor<D, R> impl;

	protected DecoratingPropertyAccessor(PropertyAccessor<D, R> impl)
	{
		super(impl.getDomain(),impl.getRange(),impl.isMultipleCardinality());
		this.impl=impl;
	}

	@Override public boolean isUnknown(Object value)
	{
		return impl.isUnknown(value);
	}
}
