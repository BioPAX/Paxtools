package org.biopax.paxtools.util;

import org.apache.commons.lang3.StringUtils;
import org.biopax.paxtools.model.BioPAXElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public enum BPCollections {
	I; // singleton

	/**
	 * This interface is responsible for setting the class
	 * and initialize and load factor for all sets and maps
	 * used in all model objects for performance purposes.
	 */
	public interface CollectionProvider {
		<R> Set<R> createSet();

		<D, R> Map<D, R> createMap();
	}

	//Default CollectionProvider implementation (HashMap, HashSet, ArrayList)
	public static class DefaultCollectionProvider implements CollectionProvider {
		public <R> Set<R> createSet() {
			return new HashSet<>();
		}

		public <D, R> Map<D, R> createMap() {
			return new HashMap<>();
		}

	}

	private CollectionProvider cProvider;
	private final Logger log = LoggerFactory.getLogger(BPCollections.class);

	BPCollections() {
		//if a custom collection provider was specified, try to use that
		String prop = System.getProperty("paxtools.CollectionProvider");
		if (StringUtils.isNotBlank(prop)) {
			try {
				Class<? extends CollectionProvider> cProviderClass = (Class<? extends CollectionProvider>) Class.forName(prop);
				cProvider = cProviderClass.getDeclaredConstructor().newInstance();
				log.info("Using custom paxtools.CollectionProvider=" + cProvider.getClass().getCanonicalName());
			} catch (Exception e) {
				log.warn("Could not initialize the specified collection provider: " + prop +
				         "; will use the default implementation; error: " + e);
			}
		}
		if(cProvider == null) {
			cProvider = new DefaultCollectionProvider();
			log.info("Using default paxtools.CollectionProvider=" + cProvider.getClass().getCanonicalName());
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

	public <R extends BioPAXElement> Set<R> createSafeSet() {
		return new BiopaxSafeSet<>(); //Set based on Map (faster, more memory, for building/merging Models)
	}

	public <D, R> Map<D, R> createMap()
	{
		return cProvider.createMap();
	}
}
