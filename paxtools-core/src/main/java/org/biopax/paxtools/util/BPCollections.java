package org.biopax.paxtools.util;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public enum BPCollections
{
	I;

	private CollectionProvider cProvider;

	private Log log = LogFactory.getLog(BPCollections.class);

	private BPCollections()
	{
		String prop = System.getProperty("paxtools.CollectionProvider");
		if (prop != null)
		{
			try
			{
				Class<? extends CollectionProvider> cProviderClass =
						(Class<? extends CollectionProvider>) Class.forName(prop);
				cProvider = cProviderClass.newInstance();
			}
			catch (IllegalAccessException | ClassNotFoundException | InstantiationException e)
			{
				log.warn("Could not initialize the specified collector provider:" + prop +
				         " . Falling back to default " +
				         "Hash based implementation. Underlying exception is " + e);

			}
		}

		if (cProvider == null) cProvider = new CollectionProvider()
		{
			@Override public <R> Set<R> createSet()
			{
				return new HashSet<>();
			}

			@Override public <D, R> Map<D, R> createMap()
			{
				return new HashMap<>();
			}
		};

	}

	/**
	 * This interface is responsible for setting the class and initialize and load factor for all sets used in all
	 * model
	 * objects for performance purposes.
	 */
	public interface CollectionProvider
	{
		public <R> Set<R> createSet();

		public <D, R> Map<D, R> createMap();
	}

	public void setProvider(CollectionProvider newProvider)
	{
		cProvider = newProvider;
	}

	public <R> Set<R> createSet()
	{
		return cProvider.createSet();
	}

	public <R extends BioPAXElement> Set<R> createSafeSet()
	{
		return new BiopaxSafeSet<R>();
	}

	public <D, R> Map<D, R> createMap()
	{
		return cProvider.createMap();
	}
}
