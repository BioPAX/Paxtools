package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;


/**
 * This is the base class for all property editors. Each property controller is responsible for
 * manipulating a certain property for a given class of objects (domain).
 */
public abstract class AbstractPropertyEditor<D extends BioPAXElement, R>
		extends SimplePropertyAccessor<D,R> implements PropertyEditor<D,R>
{
// ------------------------------ FIELDS ------------------------------

	protected static final Logger log = LoggerFactory.getLogger(AbstractPropertyEditor.class);

	/**
	 * This variable stores the method to invoke for setting a property to the to the given value. If
	 * {@link #multipleCardinality multiple cardinality}, the returned method is expected to have a
	 * {@link java.util.Set} as its only parameter.
	 */
	protected Method setMethod;

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
	 * This map keeps a list of maximum cardinality restrictions.
	 */
	private final Map<Class, Integer> maxCardinalities = new HashMap<Class, Integer>();

    public static ThreadLocal<Boolean> checkRestrictions = new ThreadLocal<Boolean>()
    {
        @Override
        protected Boolean initialValue()
        {
            return true;
        }
    };


// --------------------------- CONSTRUCTORS ---------------------------

	/**
	 * Constructor.
	 * 
	 * @param property biopax property name
	 * @param getMethod getter
	 * @param domain class the property belongs to
	 * @param range property values type/class
	 * @param multipleCardinality whether more than one value is allowed
	 */
    public AbstractPropertyEditor(String property, Method getMethod, Class<D> domain, Class<R> range,
			boolean multipleCardinality)
	{
		super(domain, range, multipleCardinality, getMethod);
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
		String def = String.format("%s %s %s", domain.getSimpleName(), property, range.getSimpleName());

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
	 * @param domain paxtools level2 interface that maps to the corresponding owl level2.
	 * @param property to be managed by the constructed controller.
	 * @param <D> domain
	 * @param <R> range
	 * @return a property controller to manipulate the beans for the given property.
	 */
	public static <D extends BioPAXElement, R> PropertyEditor<D, R> createPropertyEditor(Class<D> domain,
	                                                                                     String property)
	{
		PropertyEditor editor = null;
		try
		{
			Method getMethod = detectGetMethod(domain, property);
			boolean multipleCardinality = isMultipleCardinality(getMethod);
			Class<R> range = detectRange(getMethod);

			if (range.isPrimitive() || range.equals(Boolean.class))
			{
				editor = new PrimitivePropertyEditor<D, R>(property, getMethod, domain, range, multipleCardinality);
			} else if (range.isEnum())
			{
				editor = new EnumeratedPropertyEditor(property, getMethod, domain, range, multipleCardinality);
			} else if (range.equals(String.class))
			{
				editor = new StringPropertyEditor(property, getMethod, domain, multipleCardinality);
			} else
			{
				editor = new ObjectPropertyEditor(property, getMethod, domain, range, multipleCardinality);
			}
		}
		catch (NoSuchMethodException e)
		{
			if (log.isWarnEnabled()) log.warn("Failed creating the controller for " + property + " on " + domain);
		}
		return editor;
	}

	private static Method detectGetMethod(Class beanClass, String property) throws NoSuchMethodException
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

	/**
	 * Given the multiple cardinality feature, the range of the get method is returned.
	 * @param getMethod default method

	 * @return the range as a class
	 */
	protected static Class detectRange(Method getMethod)
	{
		Class range = getMethod.getReturnType();
		//if the return type is a collection then we have multiple cardinality
		if (Collection.class.isAssignableFrom(range))
		{
			//it is a collection, by default assume non parameterized.
			range = Object.class;
			//If the collection is parameterized, get it.
			Type genericReturnType = getMethod.getGenericReturnType();
			if (genericReturnType instanceof ParameterizedType)
			{
				try
				{
					range = (Class) ((ParameterizedType) genericReturnType).getActualTypeArguments()[0];
				}
				catch (Exception e)
				{
					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				}
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
			if (log.isTraceEnabled()) log.trace(range.getCanonicalName());
		}

		return range;
	}


	/**
	 * Detects and sets the default methods for the property to which editor is associated. If property
	 * has multiple cardinality, {@link #setMethod}, {@link #addMethod}, and {@link #removeMethod} are
	 * set, otherwise only the {@link #setMethod}.
	 * @exception NoSuchMethodException if a method for the property does not exist
	 */
	private void detectMethods() throws NoSuchMethodException
	{
		String javaName = getJavaName(property);
		if (multipleCardinality)
		{

			this.addMethod = domain.getMethod("add" + javaName, range);
			this.removeMethod = domain.getMethod("remove" + javaName, range);
		} else
		{
			this.setMethod = domain.getMethod("set" + javaName, range);
		}
	}

	// --------------------- GETTER / SETTER METHODS ---------------------

	@Override public Method getAddMethod()
	{
		return addMethod;
	}

	@Override public Method getGetMethod()
	{
		return getMethod;
	}

	@Override public String getProperty()
	{
		return property;
	}

	@Override public Method getRemoveMethod()
	{
		return removeMethod;
	}

	@Override public Method getSetMethod()
	{
		return setMethod;
	}

// --------------------- ACCESORS and MUTATORS---------------------

	// -------------------------- OTHER METHODS --------------------------

	@Override public void addMaxCardinalityRestriction(Class<? extends D> domain, int max)
	{
		if (multipleCardinality)
		{
			this.maxCardinalities.put(domain, max);
		} else
		{
			if (max == 1)
			{
				if (log.isInfoEnabled())
				{
					log.info("unnecessary use of cardinality restriction. " +
					         "Maybe you want to use functional instead?");
				}
			} else if (max == 0)
			{
				this.maxCardinalities.put(domain, max);
			} else
			{
				assert false;
			}
		}
	}

	@Override public Integer getMaxCardinality(Class<? extends D> restrictedDomain)
	{
		return this.maxCardinalities.get(restrictedDomain);
	}

	/**
	 * Checks if <em>value</em> is an instance of one of the classes given in a set. This method
	 * becomes useful, when the restrictions have to be checked for a set of objects. e.g. check if the
	 * value is in the range of the editor.
	 *
	 * @param classes a set of classes to be checked
	 * @param value value whose class will be checked
	 * @return true if value belongs to one of the classes in the set
	 */
	protected boolean isInstanceOfAtLeastOne(Set<Class<? extends BioPAXElement>> classes, Object value)
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



	@Override public R getUnknown()
	{
		return null;
	}


	@Override public void removeValueFromBean(R value, D bean)
	{
		if(value == null)
			return;

		try
		{
			if (removeMethod != null)
			{
				invokeMethod(removeMethod, bean, value);
			} else {
				assert !isMultipleCardinality() : "removeMethod is not defined " +
						"for the multiple cardinality property: " + property +
						". Here, this might add 'unknown' value while keeping exisiting one as well!";
				
				if(this.getValueFromBean(bean).contains(value))
				{
					this.setValueToBean(this.getUnknown(),bean);
				}
				else {
					log.error("Given value :" + value + 
						" is not equal to the existing value. " +
					         "remove value is ignored");
					assert getRange().isInstance(value) : "Range violation!";
					assert getDomain().isInstance(bean) : "Domain violation!";
				}
			}
		}
		catch (Exception e)
		{
			log.error("removeValueFromBean failed", e);
		}
	}


	@Override public void removeValueFromBean(Set<R> values, D bean) {
		for(R r : values) {
			removeValueFromBean(r, bean);
		}
	}
	
	
	/**
	 * Calls the <em>method</em> onto <em>bean</em> with the <em>value</em> as its parameter. In this
	 * context <em>method</em> can be one of these three: set, add, or remove.
	 * @param method method that is going to be called
	 * @param bean bean onto which the method is going to be applied
	 * @param value the value which is going to be used by method
	 */
	protected void invokeMethod(Method method, D bean, R value)
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
				message += "  Invalid domain bean: " + domain.getSimpleName() + " is not assignable from " +
				           bean.getClass();
			}
			if (!range.isAssignableFrom(value.getClass()))
			{
				message += " Invalid range value: " + range + " is not assignable from " + value.getClass();
			}
			throw new IllegalBioPAXArgumentException(message, e);
		}
		catch (Exception e) //java.lang.reflect.InvocationTargetException
		{
			String valInfo = (value == null) ? null : value.getClass().getSimpleName() + ", " + value;
			String message = "Failed to set " + property + " with " + method.getName() + " on " 
				+ domain.getSimpleName() + " (" + bean.getClass().getSimpleName() + ", " + bean + ")" 
				+ " with range: " + range.getSimpleName() + " (" + valInfo + ")";
			throw new IllegalBioPAXArgumentException(message, e);
		}
	}

	protected R parseValueFromString(String value)
	{
		throw new IllegalBioPAXArgumentException();
	}

	@Override public void setValueToBean(R value, D bean)
	{
		if (this.getPrimarySetMethod() != null)
		{
			if (log.isTraceEnabled()) {
				log.trace(this.getPrimarySetMethod().getName() + " bean:" + bean + " val:" + value);
			}
		} else {
			log.error("setMethod is null; " + " bean:" + bean + " (" + bean.getUri() + ") val:" + value);
		}

		// 'null' definitely means 'unknown' for single cardinality props
		if (value == null && !isMultipleCardinality())
			value = getUnknown(); // not null for primitive property editors

		if (value instanceof String)
		{
			value = this.parseValueFromString(((String) value));
		}
		try
		{
			if (value != null && checkRestrictions.get()) checkRestrictions(value, bean);
			invokeMethod(this.getPrimarySetMethod(), bean, value);
		}
		catch (Exception e) {
			if(log.isDebugEnabled()) {
				log.debug("setValueToBean failed to set value: " + value
						+ ((value != null) ? " of type: " + value.getClass().getSimpleName() : "")
						+ " to " + bean.getClass().getSimpleName()
						+ " (" + bean.getUri() + ") using: " + getPrimarySetMethod().getName(), e);
			} else {
				log.error("setValueToBean, failed: " + e.getMessage());
			}
		}
	}

	@Override public void setValueToBean(Set<R> values, D bean)
	{
		if (values == null) {
			setValueToBean(((R) null), bean);
		}
		else if (this.isMultipleCardinality() || values.size() < 2)
		{
			for (R r : values) {
				this.setValueToBean(r, bean);
			}
		} else {
			throw new IllegalBioPAXArgumentException(this.getProperty() +
				" is single cardinality; cannot set it with a set of size larger than 1");
		}
	}


	/**
	 * Checks if the <em>bean</em> and the <em>value</em> are consistent with the cardinality rules of
	 * the model. This method is important for validations.
	 * @param value Value that is related to the object
	 * @param bean Object that is related to the value
	 */
	protected void checkRestrictions(R value, D bean)
	{
		Integer max = this.maxCardinalities.get(value.getClass());
		if (max != null)
		{
			if (max == 0)
			{
				throw new IllegalBioPAXArgumentException("Cardinality 0 restriction violated");
			} else
			{
				assert multipleCardinality;
				Set values = this.getValueFromBean(bean);
				if (values.size() >= max)
				{
					throw new IllegalBioPAXArgumentException("Cardinality " + max + " restriction violated");
				}
			}
		}
	}

	@Override public Method getPrimarySetMethod()
	{
		return multipleCardinality ? addMethod : setMethod;
	}


}
