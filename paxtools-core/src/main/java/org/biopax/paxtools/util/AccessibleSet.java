package org.biopax.paxtools.util;

import java.util.HashSet;

public class AccessibleSet<E> extends HashSet<E>
{
	public E access(final E element)
	{
		E value = null;
		if (element != null)
		{
			final Object[] trap = new Object[]{null};
			if(this.contains(new Object()
			{
				public int hashCode()
				{
					return element.hashCode();
				}

				public boolean equals(Object other)
				{
					return other != null && element.equals(trap[0] = other);
				}
			}))
			{
				value= (E) trap[0];
			}
		}
		return value;
	}
}
