package org.biopax.paxtools.controller;

import org.apache.commons.collections15.set.CompositeSet;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.util.Set;

/**

 */
public class UnionPropertyAccessor<D extends BioPAXElement> implements PropertyAccessor<D,Object>
{
	Set<PropertyAccessor<? extends D, ?>> union;
	PropertyAccessor<? extends D, ?> first;

	private Class<D> domain;

	public UnionPropertyAccessor(Set<PropertyAccessor<? extends D, ?>> union, Class<D> domain)
	{
		this.domain = domain;
		if(union == null || union.isEmpty())
		{
			throw new IllegalBioPAXArgumentException("Empty set of editors. Can't create a union");
		}
		this.union = union;
		first = this.union.iterator().next();

	}

	@Override public Class<D> getDomain()
	{
		return domain;
	}

	@Override public Class getRange()
	{
		return first.getRange();
	}

	@Override public boolean isMultipleCardinality()
	{
		return first.isMultipleCardinality();
	}

	@Override public Set getValueFromBean(D bean) throws IllegalBioPAXArgumentException
	{
		CompositeSet valueFromBean =new CompositeSet();

		for (PropertyAccessor atomicAccessor : union)
		{
			if(atomicAccessor.getDomain().isAssignableFrom(bean.getModelInterface()))
			{
				valueFromBean.addComposited(atomicAccessor.getValueFromBean(bean));
			}
		}
		return valueFromBean;
	}

	@Override public boolean isUnknown(Object value)
	{
		return first.isUnknown(value);
	}
}
