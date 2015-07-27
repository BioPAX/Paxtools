package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;

import java.lang.reflect.Method;
import java.util.Set;

/**

 */
public interface PropertyEditor<D extends BioPAXElement, R> extends PropertyAccessor<D,R>
{
	@Override String toString();

	/**
	 * @return the add method.
	 */
	Method getAddMethod();

	/**
	 * @return the get method
	 */
	Method getGetMethod();

	/**
	 * @return the proterty name as a string
	 */
	String getProperty();

	/**
	 * @return the remove method
	 */
	Method getRemoveMethod();

	/**
	 * @return the set method
	 */
	Method getSetMethod();

	/**
	 * Sets a maximum cardinality for a domain.
	 * @param domain domain on which restriction will be set
	 * @param max cardinality
	 * @see #isMultipleCardinality()
	 */
	void addMaxCardinalityRestriction(Class<? extends D> domain, int max);

	/**
	 * Return the maximum cardinality that is defined for the property to which editor is belong.
	 * @param restrictedDomain domain to be checked for the cardinality
	 * @return an integer indicating the maximum cardinality
	 */
	Integer getMaxCardinality(Class<? extends D> restrictedDomain);

	/**
	 * Gets the unknown <em>value</em>. In an object property or enumeration
	 * context a <em>value</em> is regarded to be unknown if it is null (unset);
	 * in a primitive property context it depends (can be e.g.,
	 * {@link org.biopax.paxtools.model.BioPAXElement#UNKNOWN_FLOAT})
	 * @return null or what it means that the property value is unknown
	 */
	R getUnknown();

	/**
	 * Removes the <em>value</em> from the <em>bean</em> using the default removeMethod,
	 * if such method is defined (i.e., it's a multiple cardinality property),
	 * otherwise sets <em>unknown</em> value using {@link #setValueToBean(Object, org.biopax.paxtools.model.BioPAXElement)}
	 * (but only if )
	 *
	 * @param value to be removed from the bean
	 * @param bean bean from which the value is going to be removed
	 */
	void removeValueFromBean(R value, D bean);

	/**
	 * Removes the <em>values</em> from the <em>bean</em>
	 * using the {@link #removeValueFromBean(Object, org.biopax.paxtools.model.BioPAXElement)}
	 * for each value in the set.
	 *
	 * @param values to be removed from the bean
	 * @param bean bean from which the value is going to be removed
	 */
	void removeValueFromBean(Set<R> values, D bean);

	/**
	 * Sets the <em>value</em> to the <em>bean</em> using the default setMethod if
	 * <em>value</em> is not null.
	 * @param value to be set to the <em>bean</em>
	 * @param bean to which the <em>value</em> is to be set
	 */
	void setValueToBean(R value, D bean);

	void setValueToBean(Set<R> values, D bean);

	/**
	 * Returns the primary set method of the editor. It is the setMethod for a property of
	 * single cardinality, and the addMethod method for a property of multiple cardinality.
	 * @return the method to be primarily used for setting a value to an object.
	 */
	Method getPrimarySetMethod();
}
