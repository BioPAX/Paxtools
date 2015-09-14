package org.biopax.paxtools.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
	private final static Log LOG = LogFactory.getLog(BiopaxSafeSet.class);
	
	//initial map is to be reset to a modifiable instance on first write
	private final static Map empty = Collections.unmodifiableMap(Collections.emptyMap());

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
	
	@Override
	public boolean add(E bpe)
	{
		synchronized (map) {	
			if(map.isEmpty())
			{	//new real map instead of initial fake (empty) one
				this.map = BPCollections.I.createMap();
			}
		}
			
		String uri = bpe.getRDFId();
		
		synchronized (map) { //sync on the new map instance		
			if (!map.containsKey(uri)) {
				map.put(uri, bpe);
				return true;
			} else {
				// do not throw an ex., because duplicate attempts occur naturally
				// (e.g., same PE on both left and right sides of a reaction
				// causes same participant/participantOf is touched twice)
				LOG.debug("ignored duplicate:" + uri);
				return false;
			}
		}
	}
	
	
	@Override
	public boolean contains(Object o) {
		if(map==empty)
			return false;
		
		synchronized (map) {//to sync due to two operations
			return super.contains(o) 
				&& ( get(((E)o).getRDFId()) == o );
		}
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
