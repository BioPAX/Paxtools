package org.biopax.paxtools.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A composite iterator that iterates over multiple iterators.
 * @param <T> Base class type which all subiterator's type must extend from.
 */
public class CompositeIterator<T> implements Iterator<T>
{
    Iterator<? extends Collection<? extends T>> collectionIterator;
    Iterator<? extends T> currentIterator;

	/**
	 * This constructor creates an iterator instance from a set of collections
	 * @param collections to be iterated over.
	 */
	public CompositeIterator(Collection<? extends Collection<? extends T>> collections)
    {
        collectionIterator = collections.iterator();
        currentIterator = getNextSet();

    }

    @Override
    public boolean hasNext() {
        if (currentIterator == null) {
            return false;
        } else if (!currentIterator.hasNext())
        {
            currentIterator = getNextSet();
            return this.hasNext();
        }
        else return true;
    }

    @Override
    public T next() {
        if (this.hasNext()) {
            return currentIterator.next();
        } else {
            throw new NoSuchElementException();
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    private Iterator<? extends T> getNextSet() {
        return collectionIterator.hasNext() ? collectionIterator.next().iterator() : null;
    }

}
    

