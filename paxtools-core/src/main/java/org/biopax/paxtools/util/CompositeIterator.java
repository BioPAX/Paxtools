package org.biopax.paxtools.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class CompositeIterator<T> implements Iterator<T> 
{
    Iterator<? extends Collection<? extends T>> collectionIterator;
    Iterator<? extends T> currentIterator;

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
    

