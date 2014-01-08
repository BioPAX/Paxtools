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

	private final Log log = LogFactory.getLog(BPCollections.class);

	private BPCollections()
	{
		String prop = System.getProperty("paxtools.CollectionProvider");
		log.info("System property: paxtools.CollectionProvider=" + prop);		
		if (prop != null)
		{
			try
			{
				Class<? extends CollectionProvider> cProviderClass =
						(Class<? extends CollectionProvider>) Class.forName(prop);
				cProvider = cProviderClass.newInstance();
				log.info("CollectionProvider " + prop + " was successfully activated.");
			}
			catch (IllegalAccessException | ClassNotFoundException | InstantiationException e)
			{
				log.warn("Could not initialize the specified collector provider:" + prop +
				         " . Falling back to default " +
				         "Hash based implementation. Underlying exception is " + e);

			}
		}

		if (cProvider == null) {
			cProvider = new CollectionProvider() {
				@Override
				public <R> Set<R> createSet() {
					return new HashSet<>();
				}

				@Override
				public <D, R> Map<D, R> createMap() {
					return new HashMap<>();
				}
			};
			log.info("Using the default CollectionProvider (creates HashMap, HashSet).");
		}
		
	}

	/**
	 * This interface is responsible for setting the class 
	 * and initialize and load factor for all sets and maps 
	 * used in all model objects for performance purposes.
	 */
	public interface CollectionProvider
	{
		public <R> Set<R> createSet();

		public <D, R> Map<D, R> createMap();
	}

	/**
	 * Sets a specific {@link CollectionProvider} (for 
	 * multiple-cardinality BioPAX properties)
	 * 
	 * @param newProvider not null
	 */
	public void setProvider(CollectionProvider newProvider)
	{
		if(newProvider == null)
			throw new IllegalArgumentException();
		
		cProvider = newProvider;
		
		log.info("Using CollectionProvider: " 
				+ newProvider.getClass().getSimpleName());
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
