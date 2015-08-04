package org.biopax.paxtools.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * This class is a transitive decorator for PropertyAccessors. BioPAX has transitive properties, 
 * such as: memberEntityReference, memberEntityFeature, memberPhysicalEntity, component. 
 * These all represent nested containment relationships.
 *
 * When decorating a suitable property accessor this accessor will traverse the whole nesting hierarchy and bring all
 * children( or if inverse parents). For example when used on a {@link org.biopax.paxtools.model.level3.Complex#getComponent()}
 * it will not only return the immediate components but also the components of the components.
 */
public class TransitivePropertyAccessor<D extends BioPAXElement, R> extends DecoratingPropertyAccessor<D, R>
{
	private final static Log log = LogFactory.getLog(TransitivePropertyAccessor.class);
	
	private TransitivePropertyAccessor(PropertyAccessor<D, R> accessor)
	{
		super(accessor);
	}

	@Override public Set<? extends R> getValueFromBean(D bean) throws IllegalBioPAXArgumentException
	{
		Set<R> values = new HashSet<R>();
		Stack<BioPAXElement> visited = new Stack<BioPAXElement>();
		visited.push(bean);
		transitiveGet(bean, values, visited);
		return values;
	}


	private void transitiveGet(D bean, Set<R> values, Stack<BioPAXElement> visited)
	{
		Set<? extends R> valuesFromBean = impl.getValueFromBean(bean);
		for (R value : valuesFromBean)
		{
			values.add(value);
			if(!visited.contains(value)) {
				if(getDomain().isInstance(value)) {
					visited.push((BioPAXElement)value);
					transitiveGet((D) value, values, visited);
					visited.pop();
				}
			} else {
				//report loop (unfortunately, no way to get the biopax prop. name here)
				log.warn("Escaped an inf. loop in transitiveGet: already processed element: " 
						+ ((BioPAXElement)value).getRDFId());
			}
		}
	}

	public static <D extends BioPAXElement, R> TransitivePropertyAccessor<D, R> create(PropertyAccessor<D, R> pa)
	{
		return new TransitivePropertyAccessor<D, R>(pa);
	}

}
