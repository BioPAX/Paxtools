package org.biopax.paxtools.util;


import org.biopax.paxtools.model.BioPAXElement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class BPCollections
{


	private static CollectionProvider cProvider = new CollectionProvider()
	{
		@Override public <R> Set<R> createSet()
		{

			return new HashSet<R>();
		}

		@Override public <D, R> Map<D, R> createMap()
		{
			return new HashMap<D,R>();
		}
	};

	/**
	 * This interface is responsible for setting the class and initialize and load factor for all sets used in all model
	 * objects for performance purposes.
	 */
	public interface CollectionProvider
	{
		public <R> Set<R> createSet();

		public <D,R> Map<D,R> createMap();
	}


	public static void setProvider(CollectionProvider newProvider)
	{
		cProvider = newProvider;
	}

	public static <R> Set<R> createSet()
	{
		return cProvider.createSet();
	}

	public static <R extends BioPAXElement> Set<R> createSafeSet()
	{
		return new BiopaxSafeSet<R>();
	}

	public static <D,R> Map<D,R> createMap()
	{
		return cProvider.createMap();
	}
}
