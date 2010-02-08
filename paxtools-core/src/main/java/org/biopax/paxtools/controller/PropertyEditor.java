package org.biopax.paxtools.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * This is the base class for all property editors. Each property controller is responsible for
 * manipulating a certain property for a given class of objects (domain).
 */
public abstract class PropertyEditor<D extends BioPAXElement, R>
{
// ------------------------------ FIELDS ------------------------------

	protected static final Log log = LogFactory.getLog(PropertyEditor.class);

	/**
	 * This variable stores the method to invoke for setting a property to the to the given value. If
	 * {@link #multipleCardinality multiple cardinality}, the returned method is expected to have a
	 * {@link java.util.Set} as its only parameter.
	 */
	protected Method setMethod;

	/**
	 * This variable stores the method to invoke for getting the value of the property on a given bean.
	 * In the case of multiple cardinality, this method is expected to return a {@link java.util.Set}
	 * otherwise an instance of {@link #range}
	 */
	protected final Method getMethod;

	/**
	 * This variable stores the method to invoke for adding the given value to the property managed by
	 * this commander. In the case of multiple cardinality, the method is expected to have a {@link
	 * #range} as its only parameter, otherwise expected to be null.
	 */
	protected Method addMethod;

	/**
	 * This variable stores the method for removing the value of the property on a given bean. In the
	 * case of multiple cardinality, this method is expected to have a {@link #range} as its only
	 * parameter, otherwise expected to be null
	 */
	protected Method removeMethod;

	/**
	 * Local OWL name of the property
	 */
	protected final String property;

	/**
	 * This is the Class representing the domain of the property.
	 */
	private Class<D> domain;

	/**
	 * This is the Class representing the range of the property. It is by default an object.
	 */
	private Class<R> range;


	/**
	 * This is false if there is a cardinality restriction of one on the property.
	 */
	protected final boolean multipleCardinality;

	/**
	 * This map keeps a list of maximum cardinality restrictions.
	 */
	private final Map<Class, Integer> maxCardinalities =
			new HashMap<Class, Integer>();

// --------------------------- CONSTRUCTORS ---------------------------

	public PropertyEditor(String property, Method getMethod, Class<D> domain,
	                      Class<R> range, boolean multipleCardinality)
	{
		this.domain = domain;
		this.range = range;
		this.multipleCardinality = multipleCardinality;
		this.getMethod = getMethod;
		this.property = property;

		try
		{
			detectMethods();
		}
		catch (NoSuchMethodException e)
		{
			log.error("Failed at reflection, no method: " + e.getMessage());
		}
	}


// -------------------------- STATIC METHODS --------------------------

	@Override
	public String toString()
	{
		//todo cardinalities are not being read
		String def = domain.getSimpleName() + " " + property + " " + range.getSimpleName();
		for (Class aClass : maxCardinalities.keySet())
		{
			Integer cardinality = maxCardinalities.get(aClass);
			def += " C:" + aClass.getSimpleName() + ":" + cardinality;
		}
		return def;
	}


	/**
	 * This method creates a property reflecting on the domain and property. Proper subclass is chosen
	 * based on the range of the property.
	 *
	 * @param domain   paxtools level2 interface that maps to the corresponding owl level2.
	 * @param property to be managed by the constructed controller.
	 * @return a property controller to manipulate the beans for the given property.
	 */
	public static PropertyEditor createPropertyEditor(
			Class<? extends BioPAXElement> domain,
			String property)
	{
		PropertyEditor editor = null;
		try
		{
			Method getMethod = detectGetMethod(domain, property);
			boolean multipleCardinality = isMultipleCardinality(getMethod);
			Class range = detectRange(getMethod, multipleCardinality);

			if (range.isPrimitive() || range.equals(Boolean.class))
			{
				editor = new PrimitivePropertyEditor(property,
						getMethod,
						domain,
						range,
						multipleCardinality);
			}
			else if (range.isEnum())
			{
				editor = new EnumeratedPropertyEditor(property,
						getMethod,
						domain,
						range,
						multipleCardinality);
			}

			else if (range.equals(String.class))
			{
				editor = new StringPropertyEditor(property,
						getMethod,
						domain,
						range,
						multipleCardinality);
			}
			else
			{
				editor = new ObjectPropertyEditor(property,
						getMethod,
						domain,
						range,
						multipleCardinality);
			}
		}
		catch (NoSuchMethodException e)
		{
			log.warn("Failed creating the controller for " + property + " on " +
			         domain);
		}
		return editor;
	}

	private static Method detectGetMethod(Class beanClass, String property
	)
			throws NoSuchMethodException
	{
		String javaMethodName = getJavaName(property);
		//This is the name we are going to try, log it down
		if (log.isTraceEnabled())
		{
			log.trace("javaMethodName = get" + javaMethodName);
		}

		//extract the get method
		return beanClass.getMethod("get" + javaMethodName);
	}

	/**
	 * Given the name of a property's name as indicated in the OWL file, this method converts the name
	 * to a Java compatible name.
	 *
	 * @param owlName the property name as a string
	 * @return the Java compatible name of the property
	 */
	private static String getJavaName(String owlName)
	{
		// Since java does not allow '-' replace them all with '_'
		String s = owlName.replaceAll("-", "_");
		s = s.substring(0, 1).toUpperCase() + s.substring(1);
		return s;
	}

	private static boolean isMultipleCardinality(Method getMethod)
	{
		return Set.class.isAssignableFrom(getMethod.getReturnType());
	}

	/**
	 * Given the multiple cardinality feature, the range of the get method is returned.
	 *
	 * @param getMethod           default method
	 * @param multipleCardinality boolean value to indicate whether to consider multiple cardinality
	 * @return the range as a class
	 */
	private static Class detectRange(Method getMethod,
	                                 boolean multipleCardinality)
	{
		Class range = getMethod.getReturnType();
		//if the return type is a collection then we have multiple cardinality
		if (multipleCardinality)
		{
			//it is a collection, by default assume non parameterized.
			range = Object.class;
			//If the collection is parameterized, get it.
			Type genericReturnType = getMethod.getGenericReturnType();
			if (genericReturnType instanceof ParameterizedType)
			{
				range = (Class) ((ParameterizedType) genericReturnType)
						.getActualTypeArguments()[0];
				//Now this is required as autoboxing will not work with reflection
				if (range == Double.class)
				{
					range = double.class;
				}
				if (range == Float.class)
				{
					range = float.class;
				}
				if (range == Integer.class)
				{
					range = int.class;
				}
			}
			if (log.isTraceEnabled())
			{
				log.trace(range);
			}
		}

		return range;
	}


	/**
	 * Detects and sets the default methods for the property to which editor is associated. If property
	 * has multiple cardinality, {@link #setMethod}, {@link #addMethod}, and {@link #removeMethod} are
	 * set, otherwise only the {@link #setMethod}.
	 *
	 * @throws NoSuchMethodException if a method for the property does not exist
	 */
	private void detectMethods()
			throws NoSuchMethodException
	{
		String javaName = getJavaName(property);
		if (multipleCardinality)
		{

			this.setMethod =
					domain.getMethod("set" + javaName, Set.class);
			this.addMethod =
					domain.getMethod("add" + javaName, range);
			this.removeMethod =
					domain.getMethod("remove" + javaName, range);
		}
		else
		{
			this.setMethod =
					domain.getMethod("set" + javaName, range);
		}
	}

	// --------------------- GETTER / SETTER METHODS ---------------------

	/**
	 * Returns the {@link #addMethod}.
	 *
	 * @return the add method.
	 */
	public Method getAddMethod()
	{
		return addMethod;
	}

	/**
	 * Returns the domain of the property.
	 *
	 * @return the domain of the editor
	 */
	public Class<D> getDomain()
	{
		return domain;
	}

	/**
	 * Returns {@link #getMethod}.
	 *
	 * @return the get method
	 */
	public Method getGetMethod()
	{
		return getMethod;
	}

	/**
	 * Returns the property name.
	 *
	 * @return the proterty name as a string
	 */
	public String getProperty()
	{
		return property;
	}

	/**
	 * Returns the range of the editor.
	 *
	 * @return a class
	 */
	public Class<R> getRange()
	{
		return range;
	}

	/**
	 * Returns the {@link #removeMethod}.
	 *
	 * @return the remove method
	 */
	public Method getRemoveMethod()
	{
		return removeMethod;
	}

	/**
	 * Returns the {@link #setMethod}.
	 *
	 * @return the set method
	 */
	public Method getSetMethod()
	{
		return setMethod;
	}

// --------------------- ACCESORS and MUTATORS---------------------

	/**
	 * Checks if the property to which editor is assigned has multiple cardinality.
	 *
	 * @return true if editor belongs to a multiple cardinality property.
	 */
	public boolean isMultipleCardinality()
	{
		return multipleCardinality;
	}

// -------------------------- OTHER METHODS --------------------------

	/**
	 * Sets a maximum cardinality for a domain.
	 *
	 * @param domain domain on which restriction will be set
	 * @param max    cardinality
	 * @see #isMultipleCardinality()
	 */
	public void addMaxCardinalityRestriction(Class<? extends D> domain, int max)
	{
		if (multipleCardinality)
		{
			this.maxCardinalities.put(domain, max);
		}
		else
		{
			if (max == 1)
			{
				if (log.isInfoEnabled())
				{
					log.info(
							"unnecessary use of cardinality restriction. " +
							"Maybe you want to use functional instead?");
				}
			}
			else if (max == 0)
			{
				this.maxCardinalities.put(domain, max);
			}
			else
			{
				assert false;
			}
		}
	}

	/**
	 * Return the maximum cardinality that is defined for the property to which editor is belong.
	 *
	 * @param restrictedDomain domain to be checked for the cardinality
	 * @return an integer indicating the maximum cardinality
	 */
	public Integer getMaxCardinality(Class<? extends D> restrictedDomain)
	{
		return this.maxCardinalities.get(restrictedDomain);
	}

	/**
	 * Checks if <em>value</em> is an instance of one of the classes given in a set. This method
	 * becomes useful, when the restrictions have to be checked for a set of objects. e.g. check if the
	 * value is in the range of the editor.
	 *
	 * @param classes a set of classes to be checked
	 * @param value   value whose class will be checked
	 * @return true if value belongs to one of the classes in the set
	 */
	protected boolean isInstanceOfAtLeastOne(Set<Class> classes, Object value)
	{
		boolean check = false;
		for (Class aClass : classes)
		{
			if (aClass.isInstance(value))
			{
				check = true;
				break;
			}
		}
		return check;
	}

	/**
	 * Checks if the <em>value</em> is unkown. In this context a <em>value</em> is regarded to be
	 * unknown if it is null (unset).
	 *
	 * @param value the value to be checked
	 * @return true if the value is unknown
	 */
	public boolean isUnknown(Object value)
	{
		return value == null;
	}

	/**
	 * Removes the <em>value</em> from the <em>bean</em> using the default {@link #removeMethod}.
	 *
	 * @param value to be removed from the bean
	 * @param bean  bean from which the value is going to be removed
	 */
	public void removePropertyFromBean(BioPAXElement value, BioPAXElement bean)
	{
		try
		{
			invokeMethod(removeMethod, bean, value);
		}
		catch (Exception e)
		{
			log.error(e);
		}

	}

	/**
	 * Calls the <em>method</em> onto <em>bean</em> with the <em>value</em> as its parameter. In this
	 * context <em>method</em> can be one of these three: set, add, or remove.
	 *
	 * @param method method that is going to be called
	 * @param bean   bean onto which the method is going to be applied
	 * @param value  the value which is going to be used by method
	 */
	protected void invokeMethod(Method method, Object bean, Object value)
	{
		assert bean != null;
		try
		{
			method.invoke(domain.cast(bean), value);
		}
		catch (ClassCastException e)
		{
			String message = "Failed to set property: " + property;
			if (!domain.isAssignableFrom(bean.getClass()))
			{
				message += "  Invalid domain bean: " +
				           domain.getSimpleName() + " is not assignable from " +
				           bean.getClass();
			}
			if (!range.isAssignableFrom(value.getClass()))
			{
				message += " Invalid range value: " +
				           range + " is not assignable from " +
				           value.getClass();
			}
			throw new IllegalBioPAXArgumentException(message, e);
		}
		catch (InvocationTargetException e)
		{
			String message = "Failed to set property: " + property +
			                 " with method: " + method.getName()
			                 + " on " + domain.getSimpleName() +
			                 " (" + bean.getClass().getSimpleName() + ")" +
			                 " with range: " + range.getSimpleName() +
			                 " (" + value.getClass().getSimpleName() + ")";
			throw new IllegalBioPAXArgumentException(message, e);
		}
		catch (IllegalAccessException e)
		{
			String message = "Failed to set property: " + property +
			                 " with method: " + method.getName()
			                 + " on " + domain.getSimpleName() +
			                 " (" + bean.getClass().getSimpleName() + ")" +
			                 " with range: " + range.getSimpleName() +
			                 " (" + value.getClass().getSimpleName() + ")";
			throw new IllegalBioPAXArgumentException(message, e);
		}
	}

	/**
	 * Sets the <em>value</em> to the <em>bean</em> using the default {@link #setMethod} if
	 * <em>value</em> is not null.
	 *
	 * @param bean  to which the <em>value</em> is to be set
	 * @param value to be set to the <em>bean</em>
	 */
	public void setPropertyToBean(BioPAXElement bean, Object value)
	{
		if (log.isTraceEnabled())
		{
			log.trace(setMethod.getName()
			          + " bean:" + bean + " val:" + value);
		}
		if (value != null)
		{

			try
			{
				checkRestrictions(bean, value);
				invokeMethod(this.getPrimarySetMethod(), bean, value);
			}
			catch (Exception e)
			{
				log.error("Failed to set value: " + value + " to bean " + bean);
				log.error(e);
			}
		}
	}

	/**
	 * Checks if the <em>bean</em> and the <em>value</em> are consistent with the cardinality rules of
	 * the model. This method is important for validations.
	 *
	 * @param bean  Object that is related to the value
	 * @param value Value that is related to the object
	 */
	protected void checkRestrictions(Object bean, Object value)
	{
		Integer max = this.maxCardinalities.get(value.getClass());
		if (max != null)
		{
			if (max == 0)
			{
				throw new IllegalBioPAXArgumentException(
						"Cardinality 0 restriction violated");
			}
			else
			{
				assert multipleCardinality;
				Set values = (Set) this.getValueFromBean(bean);
				if (values.size() >= max)
				{
					throw new IllegalBioPAXArgumentException(
							"Cardinality " + max + " restriction violated");
				}
			}
		}
	}

	/**
	 * Returns the value of the <em>bean</em> using the default {@link #getMethod}.
	 *
	 * @param bean the object whose property is requested
	 * @return an object as the value
	 */
	public R getValueFromBean(Object bean) throws IllegalBioPAXArgumentException
	{
		try
		{
			return (R) this.getMethod.invoke(bean);
		}
		catch (IllegalAccessException e)
		{
			throw new IllegalBioPAXArgumentException("Could not invoke get method " +
			                                         getMethod.getName() + " for " + bean, e);
		}
		catch (InvocationTargetException e)
		{
			throw new IllegalBioPAXArgumentException("Could not invoke get method " +
			                                         getMethod.getName() + " for " + bean, e);
		}
	}

	/**
	 * Returns the primary set method of the editor. It is the {@link #setMethod} for a property of
	 * single cardinality, and the {@link #addMethod} method for a property of multiple cardinality.
	 *
	 * @return the method to be primarily used for setting a value to an object.
	 */
	public Method getPrimarySetMethod()
	{
		return multipleCardinality ? addMethod : setMethod;
	}

	/**
	 * Returns a shallow copy of the value. If this is a biopax element, links to other Biopax
	 * elements will not be preserved. Primitive fields and enums however will be cloned.
	 * @return
	 */
	public R copyValueFromBean(Object bean)
	{
		return getValueFromBean(bean);
	}
}
