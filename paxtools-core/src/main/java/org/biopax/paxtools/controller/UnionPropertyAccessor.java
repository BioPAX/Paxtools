package org.biopax.paxtools.controller;

import org.apache.commons.collections15.set.CompositeSet;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.util.Set;

/**
 *  In OWL a single property can have multiple domains. This is not supported in OO. This class amends this by
 *  using a Facade pattern. A Union property will act like a OWL property with a union domain but in fact is composed
 *  of multiple single domain properties.
 */
public class UnionPropertyAccessor<D extends BioPAXElement,R> extends PropertyAccessorAdapter<D,R>
{

	Set<PropertyAccessor<? extends D, ? extends R>> union;

		public UnionPropertyAccessor(Set<PropertyAccessor<? extends D, ? extends R>> union, Class<D> domain)
	{
		super(domain, (Class<R>) union.iterator().next().getRange(), true);
		if(union == null || union.isEmpty())
		{
			throw new IllegalBioPAXArgumentException("Empty set of editors. Can't create a union");
		}
		this.union = union;
		this.multipleCardinality = union.iterator().next().isMultipleCardinality();
	}

	@Override public Set getValueFromBean(D bean) throws IllegalBioPAXArgumentException
	{
		CompositeSet valueFromBean = new CompositeSet();

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
		return union.iterator().next().isUnknown(value);
	}
}
