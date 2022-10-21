package org.biopax.paxtools.util;

import org.biopax.paxtools.model.BioPAXElement;

import java.util.*;


/**
 * A thread-safe set of BioPAX objects that also prevents adding several elements 
 * having the same URI. It also allows to quickly get a BioPAX 
 * object by URI. This set is used internally by all multiple cardinality
 * BioPAX object property and inverse property implementations (since v4.2, 2013).
 *
 * @author rodche
 */
public class BiopaxSafeSet<E extends BioPAXElement> extends AbstractSet<E>
{
	//initial map is to be reset to a modifiable instance on first write
	private final static Map empty = Collections.emptyMap();

	private Map<String,E> map;
	
	public BiopaxSafeSet()
	{
		map = empty;
	}

	public Iterator<E> iterator() {
		synchronized (map) {
			return map.values().iterator();
		}
	}

	public int size()
	{
		synchronized (map) {
			return map.size();
		}
	}


	/**
	 * Adds a new element to the collection unless it is null, or URI is null,
	 * or already contains an element with the same URI.
	 * @param bpe
	 * @return
	 */
	@Override
	public boolean add(E bpe)
	{
		if(bpe == null || bpe.getUri() == null) {
			return false;
		}

		synchronized (map) {
			if(map.isEmpty())
			{	//new real map instead of initial fake (empty) one
				this.map = BPCollections.I.createMap();
			}
		}
			
		String uri = bpe.getUri();
		synchronized (map) { //sync on the new map instance		
			if (!map.containsKey(uri)) { //prevent replacing existing objects with the same URI
				map.put(uri, bpe);
				return true;
			} else {
				// do not throw an ex., because duplicate attempts may occur naturally
				return false;
			}
		}
	}

	
	@Override
	public boolean contains(Object o) {
		if(map == empty || !(o instanceof BioPAXElement) || ((E)o).getUri() == null) {
			return false;
		}
		
		synchronized (map) {
			return map.get(((E)o).getUri()) == o;
		}
	}
}
