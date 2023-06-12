package org.biopax.paxtools.util;

import org.biopax.paxtools.model.BioPAXElement;

import java.io.Serializable;
import java.util.*;


/**
 * A thread-safe Set of BioPAX objects that also prevents adding several elements having the same URI.
 * This is backed by an internal Map (more memory but faster ~O(1) add/contains/remove operations).
 */
public class BiopaxSafeSet<E extends BioPAXElement> extends AbstractSet<E> implements Serializable
{
	//initial map is to be reset to a modifiable instance on first write
	private final static Map empty = Collections.emptyMap();
	private Map<String,E> map;
	
	public BiopaxSafeSet() {
		map = empty;
	}

	public Iterator<E> iterator() {
		synchronized (map) {
			return map.values().iterator();
		}
	}

	public int size() {
		synchronized (map) {
			return (map == empty) ? 0 : map.size();
		}
	}

	/**
	 * Adds a new element to the collection unless it is null, wrong type, or URI is blank,
	 * or already contains an element with the same URI and model interface.
	 * #{@inheritDoc}
	 * @param bpe biopax object
	 * @return true when the element was actually added to the set
	 * @throws NullPointerException when bpe or uri is null
	 */
	@Override
	public boolean add(E bpe)
	{
		String uri = bpe.getUri();
		synchronized (map) {
			if(map == empty) {//modifiable map instead of initial empty stub
				map = BPCollections.I.createMap();
				map.put(uri, bpe);
				return true;
			}
			else if (!map.containsKey(uri)) { //prevent replacing existing objects with the same URI
				map.put(uri, bpe);
				return true;
			}
			else {
				return false; //ignored the duplicate uri el.
			}
		}
	}

	/**
	 * #{@inheritDoc}
	 * @throws NullPointerException when o or o.uri is null
	 * @throws ClassCastException when the o is not a BioPAX element
	 */
	@Override
	public boolean contains(Object o) {
		if(o == null || !(o instanceof BioPAXElement) || ((E)o).getUri() == null) {
			return false;
		}
		synchronized (map) {
			return (map == empty) ? false : map.get(((E) o).getUri()) == o; // ~O(1)
		}
	}

	/**
	 * #{@inheritDoc}
	 * @throws NullPointerException when o or o.uri is null
	 * @throws ClassCastException when the o is not a BioPAX element
	 */
	@Override
	public boolean remove(Object o) { // ~O(1)
		synchronized (map) {
			if (contains(o)) {
				return map.remove(((E) o).getUri()) != null;
			}
		}
		return false;
	}

	/**
	 * Gets a BioPAX element by URI.
	 *
	 * @param uri absolute URI of a BioPAX individual
	 * @return BioPAX object or null
	 */
	public E get(String uri) {
		if(map==empty)
			return null;
		synchronized (map) {
			return map.get(uri);
		}
	}
}
