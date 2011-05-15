package org.biopax.paxtools.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Base class for implementing various filter sets.
 * Filter sets are unmodifiable.
 */

public abstract class AbstractFilterSet<F, E> extends AbstractSet<E> implements Filter<F>
{
	final Log log = LogFactory.getLog(ClassFilterSet.class);

	protected final Set<? extends F> baseSet;


	public AbstractFilterSet(Set<? extends F> baseSet)
	{
		this.baseSet = baseSet;
	}

	/**
	 * This size operation runs on O(n) and should be avoided for large sets.
	 * It is possible to write a one pass more efficient abstract set but its
	 * initialization cost would be higher.
	 *
	 * @return the size of the list
	 */
	public int size()
	{
		int i = 0;
		for (E e : this)
		{
			i++;
		}
		return i;
	}

	public boolean contains(Object o)
	{

		return  baseSet.contains(o) && filter(((F) o));
	}

	public Iterator<E> iterator()
	{
		return new FilterIterator(baseSet.iterator());
	}



	private class FilterIterator implements Iterator<E>
	{
		E next = null;
		final Iterator<? extends F> base;

		public FilterIterator(Iterator<? extends F> base)
		{
			this.base = base;
			fetchNext();
		}

		private void fetchNext()
		{
			F check;
			next = null;
			while (base.hasNext())
			{
				check = base.next();
				if (filter(check))
				{
					try
					{
						next = (E) check;
						break;
					}
					catch (ClassCastException ce)
					{
						log.error("wrong use of filter set. Skipping.", ce);
					}
					return;
				}
			}
		}

		public boolean hasNext()
		{
			return next != null;
		}

		public E next()
		{
			if (hasNext())
			{
				E value = next;
				fetchNext();
				return value;
			}
			else
			{
				throw new NoSuchElementException();
			}
		}

		public void remove()
		{
			throw new UnsupportedOperationException();
		}
	}
}
