package org.biopax.paxtools.trove;


import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import org.biopax.paxtools.util.BPCollections;

import java.util.Map;
import java.util.Set;

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

