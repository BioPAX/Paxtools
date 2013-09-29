package org.biopax.paxtools.util;

import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;


/**
 * A set of BioPAX objects that prevents adding several elements 
 * having the same URI. It also allows to quickly get a BioPAX 
 * object by URI. This set is used internally by multiple cardinality
 * object biopax property and inverse implementations (since v4.2, 2013).
 *
 * @param <E>
 */
public class BiopaxSafeSet<E extends BioPAXElement> extends AbstractSet<E>
{
	private final static Log LOG = LogFactory.getLog(BiopaxSafeSet.class);
	private final Map<String,E> map;
	
	public BiopaxSafeSet() {
		map = new HashMap<String, E>();
	}
	
	@Override
	public Iterator<E> iterator() {
		return map.values().iterator();
	}

	@Override
	public int size() {
		return map.size();
	}
	
	@Override
	public boolean add(E bpe) {
		String uri = bpe.getRDFId();
		if(!map.containsKey(uri)) {
			map.put(uri, bpe);
			return true;
		} else { 
			//do not throw an ex., because duplicate attempts occur naturally 
			// (e.g., same PE on both left and right sides of a reaction 
			// causes same participant/participantOf is touched twice) 
			LOG.debug("ignored duplicate:" + bpe.getRDFId());
			return false;
		}
	}
	
	
	@Override
	public boolean contains(Object o) {
		return super.contains(o) 
				&& ( get(((E)o).getRDFId()) == o );
	}
	
	
	/**
	 * Gets a BioPAX element by URI.
	 * 
	 * @param uri
	 * @return BioPAX object or null
	 */
	public E get(String uri) {
		return map.get(uri);
	}
}
