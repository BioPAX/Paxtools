package org.biopax.paxtools.util;

import org.biopax.paxtools.model.BioPAXElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public enum BPCollections
{
	I; // singleton

	/**
	 * This interface is responsible for setting the class
	 * and initialize and load factor for all sets and maps
	 * used in all model objects for performance purposes.
	 */
	public interface CollectionProvider
	{
		<R> Set<R> createSet();

		<D, R> Map<D, R> createMap();

		<D, R> Map<D, R> createMap(int initSz);

		<R> Collection<R> createCollection();

		<R> Collection<R> createCollection(int initSz);
	}

	private CollectionProvider cProvider;

	private final Logger log = LoggerFactory.getLogger(BPCollections.class);


	BPCollections()
	{
		String prop = System.getProperty("paxtools.CollectionProvider");
		log.info("System property: paxtools.CollectionProvider=" + prop);		
		if (prop != null)
		{
			try
			{
				Class<? extends CollectionProvider> cProviderClass =
						(Class<? extends CollectionProvider>) Class.forName(prop);
				cProvider = cProviderClass.getDeclaredConstructor().newInstance();
				log.info("CollectionProvider " + prop + " was successfully activated.");
			}
			catch (Exception e)
			{
				log.warn("Could not initialize the specified collector provider:" + prop +
				         " . Falling back to default " +
				         "Hash based implementation. Underlying exception is " + e);

			}
		}

		if (cProvider == null) {
			//Use the default CollectionProvider implementation (HashMap, HashSet, ArrayList)
			cProvider = new CollectionProvider() {
				public <R> Set<R> createSet() {
					return new HashSet<>();
				}

				public <D, R> Map<D, R> createMap() {
					return new HashMap<>();
				}

				public <D, R> Map<D, R> createMap(int initSz) {
					return new HashMap<>(initSz);
				}

				public <R> Collection<R> createCollection(int initSz) {
					return new ArrayList<>(initSz);
				}

				public <R> Collection<R> createCollection() {
					return new ArrayList<>();
				}
			};
			log.info("Using the default CollectionProvider.");
		}
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

	public <D, R> Map<D, R> createMap(int initSize)
	{
		return cProvider.createMap(initSize);
	}

	public <R> Collection<R> createCollection()
	{
		return cProvider.createCollection();
	}

	public <R> Collection<R> createCollection(int initSize)
	{
		return cProvider.createCollection(initSize);
	}

	public <R extends BioPAXElement> Collection<R> createSafeCollection()
	{
		return new BiopaxSafeCollection<>();
	}

	public <R extends BioPAXElement> Collection<R> createSafeCollection(int initSz)
	{
		return new BiopaxSafeCollection<>(initSz);
	}

}
