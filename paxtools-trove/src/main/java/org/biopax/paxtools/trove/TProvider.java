package org.biopax.paxtools.trove;


import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import org.biopax.paxtools.util.BPCollections;

import java.util.Map;
import java.util.Set;

/**
 * A Trove4j based java collections (Set, Map) provider for the paxtools-core BioPAX model.
 *
 * Enabled by adding the JVM option:
 * -Dpaxtools.CollectionProvider=org.biopax.paxtools.trove.TProvider
 * when running Paxtools or another paxtools-based JAR.
 */
public class TProvider implements BPCollections.CollectionProvider
{

	public <R> Set<R> createSet()
	{
		return new THashSet<R>();
	}

	public <D,R > Map<D,R> createMap()
	{
		return new THashMap<D, R>(4);
	}
}

