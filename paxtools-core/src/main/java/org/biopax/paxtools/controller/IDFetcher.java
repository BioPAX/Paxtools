package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;

import java.util.Set;

/**
 * @author Ozgun Babur
 */
public interface IDFetcher
{
	/**
	 * Finds a String ID for the given element.
	 * @param ele element to fetch the ID from
	 * @return some identifiers
	 */
	Set<String> fetchID(BioPAXElement ele);
}
