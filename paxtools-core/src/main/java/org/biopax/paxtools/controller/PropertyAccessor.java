package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.util.Set;

/**
 * Allows generic access to the properties or a path of properties from a bean.
 */
public interface PropertyAccessor<D extends BioPAXElement, R>
{
	/**
	 * Returns the domain of the property.
	 * @return the domain of the editor
	 */
	Class<D> getDomain();

	/**
	 * Returns the range of the editor.
	 * @return a class
	 */
	Class<R> getRange();

	/**
	 * Checks if the property to which editor is assigned has multiple cardinality.
	 * @return true if editor belongs to a multiple cardinality property.
	 */
	boolean isMultipleCardinality();

	/**
	 * Returns the value of the <em>bean</em> using the default {@link #getMethod}.
	 *
	 * @param bean the object whose property is requested
	 * @return an object as the value
	 */

	Set<? extends R> getValueFromBean(D bean) throws IllegalBioPAXArgumentException;

	/**
	 * Checks if the <em>value</em> is unkown. In this context a <em>value</em> is regarded to be
	 * unknown if it is null (unset).
	 * @param value the value to be checked
	 * @return true if the value is unknown
	 */
	public boolean isUnknown(Object value);

}
