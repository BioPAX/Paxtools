package org.biopax.paxtools.impl;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.biopax.paxtools.model.BioPAXElement;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class BioPAXElementImpl implements BioPAXElement
{
	private String uri;

	// anything extra can be stored in this map (not to persist in a DB usually)
	private Map<String, Object> annotations;

	public BioPAXElementImpl() {
		this.annotations = Collections.emptyMap(); //lazy-replace later on with a new writable map
	}

	public boolean isEquivalent(BioPAXElement element)
	{
		return this.equals(element) || this.semanticallyEquivalent(element);
	}

	protected boolean semanticallyEquivalent(BioPAXElement element)
	{
		return false;
	}

	public int equivalenceCode()
	{
		return uri.hashCode();
	}

	public String getUri()
	{
		return uri;
	}

	/**
	 * Private setter for the element's absolute URI.
	 *
	 * Normally, URI should never be modified
	 * after the object is created unless you know
	 * what you're doing (and can use Java Reflection).
	 *
	 * @param uri new absolute URI
	 */
	@SuppressWarnings("unused")
	private void setUri(String uri)
	{
		if(uri == null)
			throw new IllegalArgumentException();

		this.uri = uri;
	}


	public String toString()
	{
		return uri;
	}


	public Map<String, Object> getAnnotations() {
		synchronized (annotations) {
			if(annotations.isEmpty()) {
				annotations = new HashMap<>();
			}
			return annotations;
		}
	}


	/**
	 * true if and only if the other obj has the same biopax type 
	 * (same {@link #getModelInterface()}, not a subclass) and 
	 * same URI. Other properties are not considered.
	 */
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof BioPAXElement) &&
			new EqualsBuilder()
				.append(getModelInterface(),((BioPAXElement) obj).getModelInterface())
				.append(uri,((BioPAXElement) obj).getUri())
				.build();
	}


	/**
	 * This method is consistent with the 
	 * overridden {@link #equals(Object)} method
	 * (biopax type and URI are what matters) 
	 */
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
			.append(getModelInterface())
			.append(uri)
			.hashCode();
	}

}
