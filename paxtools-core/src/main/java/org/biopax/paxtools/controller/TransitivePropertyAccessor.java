package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.util.HashSet;
import java.util.Set;

/**
 * This class is a transitive decorator for PropertyAccessors
 */
public class TransitivePropertyAccessor<D extends BioPAXElement, R> extends DecoratingPropertyAccessor<D, R>
{
	private TransitivePropertyAccessor(PropertyAccessor<D, R> accessor)
	{

		super(accessor);
		if (!accessor.getRange().isAssignableFrom(accessor.getDomain()))
			throw new IllegalBioPAXArgumentException(); //TODO change exception

	}

	@Override public Set<? extends R> getValueFromBean(D bean) throws IllegalBioPAXArgumentException
	{
		Set<R> values = new HashSet<R>();

		transitiveGet(bean, values);
		return values;
	}

	private void transitiveGet(D bean, Set<R> values)
	{

		Set<? extends R> valuesFromBean = impl.getValueFromBean(bean);
		for (R value : valuesFromBean)
		{
			values.add(value);

			transitiveGet((D) value, values);

		}
	}

	public static <D extends BioPAXElement, R> TransitivePropertyAccessor<D, R> create(PropertyAccessor<D, R> pa)
	{
		return new TransitivePropertyAccessor<D, R>(pa);
	}

}
