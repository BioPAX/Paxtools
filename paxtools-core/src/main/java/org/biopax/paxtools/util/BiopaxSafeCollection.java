package org.biopax.paxtools.util;

import org.biopax.paxtools.model.BioPAXElement;

import java.util.*;

/**
 * A thread-safe, not necessarily ordered, collection of unique BioPAX elements
 * to be used mostly with add and iterator methods,
 * rarely - remove or clear, and never insert or get.
 */
public class BiopaxSafeCollection<E extends BioPAXElement> extends AbstractCollection<E>
{
	//initial map is to be reset to a modifiable instance on first write
	private final static Collection empty = Collections.emptyList();

	private Collection<E> c_;
	private int l_;

	public BiopaxSafeCollection() {
		c_ = empty;
		l_ = 10;
	}

	@Override
	public Iterator<E> iterator() {
		return c_.iterator();
	}

	public BiopaxSafeCollection(int initialCapacity) {
		c_ = empty;
		l_ = initialCapacity;
	}

	public int size() {
		synchronized (c_) {
			return c_.size();
		}
	}
	
	@Override
	public boolean add(E bpe)
	{
		if(bpe == null) {
			return false;
		}

		synchronized (c_) {
			if(c_ == empty) {	//replace the unmodifiable empty list
				this.c_ = BPCollections.I.createCollection(l_);
			}
		}
		
		synchronized (c_) {
			if(!c_.contains(bpe)) {
				c_.add(bpe);
				return true;
			}
			return false;
		}
	}

	@Override
	public boolean contains(Object o) {
		if (c_ == empty || !(o instanceof BioPAXElement) || ((E)o).getUri() == null) {
			return false;
		}

		synchronized (c_) {
			return super.contains(o); //~O(N); uses iterator() and E.equals() (compares BioPAX URI and interface)
		}
	}
	
}
