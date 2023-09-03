package org.biopax.paxtools.util;

import org.biopax.paxtools.model.BioPAXElement;

import java.io.Serializable;
import java.util.*;

/**
 * Another thread-safe Set of BioPAX objects that prevents adding several elements with the same URI.
 * This is backed by an internal List (less memory but slower ~O(N) add/contains/remove operations).
 */
public class BiopaxElements<E extends BioPAXElement> extends AbstractSet<E> implements Serializable
{
	//initial map is to be reset to a modifiable instance on first write
	private final static Collection empty = Collections.emptyList();
	private Collection<E> c_; //list
	private int l_; //initial size

	public BiopaxElements() {
		c_ = empty;
		l_ = 2; //instead of the default 10
	}

	public BiopaxElements(int initialCapacity) {
		c_ = empty;
		l_ = initialCapacity;
	}

	public synchronized Iterator<E> iterator() {
			return c_.iterator();
	}

	public synchronized int size() {
		return (c_ == empty) ? 0 : c_.size();
	}

	/**
	 * Adds a new element to the collection unless it is null or URI is blank,
	 * or already contains an (equal) element with the same URI and model interface.
	 * #{@inheritDoc}
	 * @param bpe
	 * @return true when the element was actually added to the set
	 * @throws NullPointerException when bpe or uri is null
	 */
	@Override
	public synchronized boolean add(E bpe) {
		if (c_ == empty) { //replace the unmodifiable empty list
			c_ = BPCollections.I.createCollection(l_);
			return c_.add(bpe);
		}
		else if(!containsUri(bpe.getUri())) { //no duplicates (this is a Set backed by internal List)
			return c_.add(bpe);
		}
		else {
			return false;
		}
	}

	private synchronized boolean containsUri(String uri) {
		if (c_ != empty) {
			Iterator<E> it = iterator();
			while (it.hasNext())
				if (uri.equals(it.next().getUri()))
					return true;
		}
		return false;
	}

	@Override
	public synchronized boolean contains(Object o) {
		if (c_ != empty) {
			Iterator<E> it = iterator();
			while (it.hasNext())
				if (o == it.next())
					return true;
		}
		return false;
	}

	/**
	 * #{@inheritDoc}
	 * @throws NullPointerException when o or o.uri is null
	 * @throws ClassCastException when the o is not a BioPAX element
	 */
	@Override
	public synchronized boolean remove(Object o) {
		if (c_ != empty) {
			Iterator<E> it = iterator();
			while (it.hasNext())
				if (o == it.next()) {
					it.remove();
					return true;
				}
		}
		return false;
	}

}
