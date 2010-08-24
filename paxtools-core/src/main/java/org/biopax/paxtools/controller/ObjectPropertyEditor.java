package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.util.AutoComplete;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Provides an editor compatible with all value types other Primitive, ENUM, and String by extending
 * the {@link org.biopax.paxtools.controller.PropertyEditor}.
 *
 * @see org.biopax.paxtools.controller.PropertyEditor
 */
public class ObjectPropertyEditor<D extends BioPAXElement, R extends BioPAXElement>
		extends PropertyEditor<D, R>
{
// ------------------------------ FIELDS ------------------------------

	private final HashMap<Class, Set<Class>> restrictedRanges =
			new HashMap<Class, Set<Class>>();

	private Method inverseGetMethod;

	private boolean inverseMultipleCardinality;

	private boolean completeForward;
	private boolean completeBackward;

// --------------------------- CONSTRUCTORS ---------------------------
//todo
//	@Override
//	public String toString()
//	{
//		String s = super.toString();
//		Set<Class> ranges = restrictedRanges.get(domain);
//		if (ranges != null)
//
//		{
//			s += " R";
//			for (Class aClass1 : ranges)
//			{
//				s += ":" + aClass1.getSimpleName();
//			}
//		}
//		return s;
//	}

	public ObjectPropertyEditor(String property, Method getMethod,
	                            Class<D> domain,
	                            Class<R> range,
	                            boolean multipleCardinality)
	{
		super(property,
				getMethod,
				domain,
				range,
				multipleCardinality);

		inverseGetMethod = findInverseGetMethod();

		if (inverseGetMethod != null)
		{
			inverseMultipleCardinality = isMultipleCardinality(inverseGetMethod);
		}

		AutoComplete autComp = getGetMethod().getAnnotation(AutoComplete.class);

		if (autComp == null)
		{
			completeForward = true;
			completeBackward = false;
		}
		else
		{
			completeForward = autComp.forward();
			completeBackward = autComp.backward();
		}
	}

// --------------------- GETTER / SETTER METHODS ---------------------

	public HashMap<Class, Set<Class>> getRestrictedRanges()
	{
		return restrictedRanges;
	}

	public boolean isCompleteForward()
	{
		return completeForward;
	}

	public boolean isCompleteBackward()
	{
		return completeBackward;
	}

	public boolean isInverseMultipleCardinality()
	{
		return inverseMultipleCardinality;
	}

	public Method getInverseGetMethod()
	{
		return inverseGetMethod;
	}

	// -------------------------- OTHER METHODS --------------------------

	public void addRangeRestriction(Class domain, Set<Class> ranges)
	{
		this.restrictedRanges.put(domain, ranges);
	}

	@Override
	protected void checkRestrictions(D bean, R value)
	{
		super.checkRestrictions(bean, value);
		Set<Class> classes = getRestrictedRangesFor((Class<? extends D>) bean.getModelInterface());
		if (classes != null && !isInstanceOfAtLeastOne(classes, value))
		{
			throw new IllegalBioPAXArgumentException(
					"The range restriction is violated \n" +
					"value: " + value + "--> bean: " + bean);
		}
	}

	public Set<Class> getRestrictedRangesFor(Class<? extends D> restrictedDomain)
	{
		Set<Class> classes = this.restrictedRanges.get(restrictedDomain);
		if (classes == null)
		{
			classes = new HashSet<Class>();
			classes.add(this.getRange());
		}
		return classes;
	}

	public boolean hasInverseLink()
	{
		return getInverseGetMethod() != null;
	}

	protected Method findInverseGetMethod()
	{
		String name = getGetMethod().getName() + "Of";

		Method method = null;

		try
		{
			method = getRange().getMethod(name);
		}
		catch (NoSuchMethodException e)
		{
			log.debug("Range " + getRange() + " has no inverse method named " + name);
		}

		return method;
	}

	public Object getInverseValueFromBean(R bean)
	{
		try
		{
			return getInverseGetMethod().invoke(bean);
		}
		catch (IllegalAccessException e)
		{
			throw new IllegalBioPAXArgumentException("Could not invoke inverse get method " +
				getInverseGetMethod().getName() + " for " + bean, e);
		}
		catch (InvocationTargetException e)
		{
			throw new IllegalBioPAXArgumentException("Could not invoke inverse get method " +
				getInverseGetMethod().getName() + " for " + bean, e);
		}
		catch (ClassCastException e) {
			throw new IllegalBioPAXArgumentException("Could not invoke inverse get method " +
					getInverseGetMethod().getName() + " for " + bean, e);
		}
	}

	// --------------------- ACCESORS and MUTATORS---------------------
}
