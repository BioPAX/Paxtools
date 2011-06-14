package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.util.AutoComplete;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Provides an editor compatible with all value types other Primitive, ENUM, and String by extending
 * the {@link org.biopax.paxtools.controller.PropertyEditor}.
 * @see org.biopax.paxtools.controller.PropertyEditor
 */
public class ObjectPropertyEditor<D extends BioPAXElement, R extends BioPAXElement> extends PropertyEditor<D, R>
{
// ------------------------------ FIELDS ------------------------------

	private Map<Class<? extends BioPAXElement>, Set<Class<? extends BioPAXElement>>> restrictedRanges =
			new HashMap<Class<? extends BioPAXElement>, Set<Class<? extends BioPAXElement>>>();

	private Method inverseGetMethod;

	private boolean inverseMultipleCardinality;

	private boolean completeForward;

	private boolean completeBackward;

	private PropertyAccessor<R, ? super D> inverseAccessor;

// --------------------------- CONSTRUCTORS ---------------------------

	public ObjectPropertyEditor(String property, Method getMethod, final Class<D> domain, final Class<R> range,
	                            boolean multipleCardinality)
	{
		super(property, getMethod, domain, range, multipleCardinality);

		inverseGetMethod = findInverseGetMethod();

		if (inverseGetMethod != null)
		{
			inverseMultipleCardinality = isMultipleCardinality(inverseGetMethod);
			this.inverseAccessor = buildInverse(detectRange(inverseGetMethod),range);
		}


		AutoComplete autComp = getGetMethod().getAnnotation(AutoComplete.class);

		if (autComp == null)
		{
			completeForward = true;
			completeBackward = false;
		} else
		{
			completeForward = autComp.forward();
			completeBackward = autComp.backward();
		}


	}

	private <T extends BioPAXElement> SimplePropertyAccessor<R, ? super D> buildInverse(Class<T> inverseRange,
	                                                                                    Class<R> inverseDomain)
	{
		return (SimplePropertyAccessor<R, ? super D>) new SimplePropertyAccessor<R, T>(inverseDomain, inverseRange,
		                                                                               inverseMultipleCardinality,
		                                                                               inverseGetMethod);
	}

// --------------------- GETTER / SETTER METHODS ---------------------

	public Map<Class<? extends BioPAXElement>, Set<Class<? extends BioPAXElement>>> getRestrictedRanges()
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

	public PropertyAccessor<R, ? super D> getInverseAccessor()
	{
		return (PropertyAccessor<R, ? super D>) inverseAccessor;
	}

	// -------------------------- OTHER METHODS --------------------------

	@Override public String toString()
	{
		StringBuilder sb = new StringBuilder(super.toString());
		for (Class rDomain : restrictedRanges.keySet())
		{
			sb.append(" D:").append(rDomain.getSimpleName()).append("=");
			String delim = "";
			for (Class<? extends BioPAXElement> range : restrictedRanges.get(rDomain))
			{
				sb.append(delim).append(range.getSimpleName());
				delim = ",";
			}

		}
		return sb.toString();
	}

	public void addRangeRestriction(Class<? extends BioPAXElement> domain, Set<Class<? extends BioPAXElement>> ranges)
	{
		this.restrictedRanges.put(domain, ranges);
	}

	public void setRangeRestriction(
			Map<Class<? extends BioPAXElement>, Set<Class<? extends BioPAXElement>>> restrictedRanges)
	{
		this.restrictedRanges = restrictedRanges;
	}

	@Override
	protected void checkRestrictions(R value, D bean)
	{
		super.checkRestrictions(value, bean);
		Set<Class<? extends BioPAXElement>> classes = getRestrictedRangesFor(
				(Class<? extends D>) bean.getModelInterface());
		if (classes != null && !isInstanceOfAtLeastOne(classes, value))
		{
			throw new IllegalBioPAXArgumentException(
				"The range restriction is violated; " 
					+ "property: " + property
					+ ", bean: " + bean + "--> value: " + value);
		}
	}

	public Set<Class<? extends BioPAXElement>> getRestrictedRangesFor(Class<? extends D> restrictedDomain)
	{
		Set<Class<? extends BioPAXElement>> classes = this.restrictedRanges.get(restrictedDomain);
		if (classes == null)
		{
			classes = new HashSet<Class<? extends BioPAXElement>>();
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


	// --------------------- ACCESORS and MUTATORS---------------------

}
