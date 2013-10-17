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
 * Provides an editor for  all object value types, e.g. everything other than Primitive, ENUM, and String.
 */
public class ObjectPropertyEditor<D extends BioPAXElement, R extends BioPAXElement> extends AbstractPropertyEditor<D,
		R>
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

	/**
	 * Full constructor.
	 * @param property Name of the property, e.g. entityReference.
	 * @param getMethod A "Method" class that represents the getter method. e.g. getEntityReference()
	 * @param domain name of the domain of this property. e.g. PhysicalEntity
	 * @param range name of the range of this property. e.g. EntityReference.
	 * @param multipleCardinality false if this property is functional, e.g. many-to-one or one-to-one.
	 */
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
		return inverseAccessor;
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

	/**
	 * This method adds a range restriction to the property editor. e.g. All entityReferences of Proteins should be
	 * ProteinReferences.
	 *
	 * Note: All restrictions specified in the BioPAX specification is automatically created by the {@link EditorMap}
	 * during initialization. Use this method if you need to add restrictions that are not specified in
	 * the model.
	 * @param domain subdomain of the property to be restricted
	 * @param ranges valid ranges for this subdomain.
	 */
	public void addRangeRestriction(Class<? extends BioPAXElement> domain, Set<Class<? extends BioPAXElement>> ranges)
	{
		this.restrictedRanges.put(domain, ranges);
	}

	/**
	 * This method sets all range restrictions.
	 *
	 * Note: All restrictions specified in the BioPAX specification is automatically created by the {@link EditorMap}
	 * during initialization. Use this method if you need to add restrictions that are not specified in
	 * the model.

	 * @param restrictedRanges a set of range restrictions specified as a map.
	 */
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

	/**
	 *
	 * @param restrictedDomain a subdomain that is restricted.
	 * @return the range restrictions for the given subdomain for this propertyEditor.
	 */
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

	/**
	 * @return true iff this property has a defined inverse link in paxtools.
	 */
	public boolean hasInverseLink()
	{
		return getInverseGetMethod() != null;
	}

	/**
	 * @return the inverse get method for this property. If the property for this editor is entityReference this method
	 * will return a Method instance that represents {@link org.biopax.paxtools.model.level3
	 * .EntityReference#getEntityReferenceOf()}.
	 */
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
