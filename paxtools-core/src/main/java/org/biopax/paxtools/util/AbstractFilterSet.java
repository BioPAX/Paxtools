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
public abstract class AbstractFilterSet<E> extends AbstractSet<E>
{
// ------------------------------ FIELDS ------------------------------

	final Log log = LogFactory.getLog(AbstractFilterSet.class);
	private final Set baseSet;

// --------------------------- CONSTRUCTORS ---------------------------

	protected AbstractFilterSet(Set baseSet)
	{
		this.baseSet = baseSet;
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface Collection ---------------------

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
		return filter(o) && baseSet.contains(o);
	}

// --------------------- Interface Iterable ---------------------

	public Iterator<E> iterator()
	{
		return new FilterIterator<E>(baseSet.iterator());
	}

// -------------------------- OTHER METHODS --------------------------

	protected abstract boolean filter(Object value);

// -------------------------- INNER CLASSES --------------------------

	private class FilterIterator<E> implements Iterator<E>
	{
		E next = null;
		final Iterator base;

		public FilterIterator(Iterator base)
		{
			this.base = base;
			fetchNext();
		}

		private void fetchNext()
		{
			Object check;
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
