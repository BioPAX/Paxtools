package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.util.HashSet;
import java.util.Set;

/**
 * This class is a transitive decorator for PropertyAccessors. BioPAX has three transitive properties:
 * memberEntityReference, memberEntityFeature, component. These all represent nested containment
 * relationships.
 *
 * When decorating a suitable property accessor this accessor will traverse the whole nesting hierarchy and bring all
 * children( or if inverse parents). For example when used on a {@link org.biopax.paxtools.model.level3.Complex#getComponent()}
 * it will not only return the immediate components but also the components of the components.
 */
public class TransitivePropertyAccessor<D extends BioPAXElement, R> extends DecoratingPropertyAccessor<D, R>
{
	private TransitivePropertyAccessor(PropertyAccessor<D, R> accessor)
	{
		super(accessor);
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
