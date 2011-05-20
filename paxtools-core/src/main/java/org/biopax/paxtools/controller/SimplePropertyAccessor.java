package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;

/**

 */
public class SimplePropertyAccessor<D extends BioPAXElement, R> extends PropertyAccessorAdapter<D, R>
{
	/**
	 * This variable stores the method to invoke for getting the value of the property on a given bean.
	 * In the case of multiple cardinality, this method is expected to return a {@link java.util.Set}
	 * otherwise an instance of {@link #range}
	 */
	protected final Method getMethod;

	public SimplePropertyAccessor(Class<D> domain, Class<R> range, boolean multipleCardinality, Method getMethod)
	{
		super(domain, range, multipleCardinality);
		this.getMethod = getMethod;
	}

	protected static boolean isMultipleCardinality(Method getMethod)
	{
		return Set.class.isAssignableFrom(getMethod.getReturnType());
	}

	/**
	 * Returns the value of the <em>bean</em> using the default {@link #getMethod}.
	 * If the value is unknown returns null or an empty set depending on the cardinality.
	 * @param bean the object whose property is requested
	 * @return an object as the value
	 */
	@Override public Set<R> getValueFromBean(D bean) throws IllegalBioPAXArgumentException
	{
		Object value;
		try
		{
			value = this.getMethod.invoke(bean);
		}
		catch (IllegalAccessException e)
		{
			throw new IllegalBioPAXArgumentException(
					"Could not invoke get method " + getMethod.getName() + " for " + bean, e);
		}
		catch (IllegalArgumentException e)
		{
			throw new IllegalBioPAXArgumentException(
					"Could not invoke get method " + getMethod.getName() + " for " + bean, e);
		}

		catch (InvocationTargetException e)
		{
			throw new IllegalBioPAXArgumentException(
					"Could not invoke get method " + getMethod.getName() + " for " + bean, e);
		}

		if (value == null) return Collections.emptySet();
		else if (this.isMultipleCardinality())
		{
			return (Collections.unmodifiableSet(((Set<R>) value)));
		}
		else
		{
			return Collections.singleton(((R) value));
		}
	}

	/**
	 * Checks if the <em>value</em> is unkown. In this context a <em>value</em> is regarded to be
	 * unknown if it is null (unset).
	 * @param value the value to be checked
	 * @return true if the value is unknown
	 */
	public boolean isUnknown(Object value)
	{
		return value == null || (value instanceof Set ? ((Set) value).isEmpty() : false);
	}
}
