package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * This class is a transitive decorator for PropertyAccessors. BioPAX has transitive properties, 
 * such as: memberEntityReference, memberEntityFeature, memberPhysicalEntity, component. 
 * These all represent nested containment relationships.
 *
 * When decorating a suitable property accessor this accessor will traverse the whole nesting hierarchy and bring all
 * children( or if inverse parents). For example when used on a {@link org.biopax.paxtools.model.level3.Complex#getComponent()}
 * it will not only return the immediate components but also the components of the components.
 */
public class TransitivePropertyAccessor<R extends BioPAXElement, D extends R> extends DecoratingPropertyAccessor<D, R>
{
	private final static Logger log = LoggerFactory.getLogger(TransitivePropertyAccessor.class);
	
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
			if(values.add(value))
			{
				//if(impl.getDomain().isInstance(value)) - unnecessary as the impl does that check
				transitiveGet((D) value, values);
			}
			else {
				//report loop
				log.debug("Escaped an inf. loop (" + impl+ ") at " + value.getUri());
			}
		}
	}

	public static < R extends BioPAXElement, D extends R> TransitivePropertyAccessor<R, D> create(
			PropertyAccessor<D, R> pa)
	{
		return new TransitivePropertyAccessor<R,D>(pa);
	}

}
