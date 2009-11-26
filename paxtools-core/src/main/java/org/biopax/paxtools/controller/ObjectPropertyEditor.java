package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

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
public class ObjectPropertyEditor extends PropertyEditor
{
// ------------------------------ FIELDS ------------------------------

	private final HashMap<Class, Set<Class>> restrictedRanges =
			new HashMap<Class, Set<Class>>();

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
	                            Class<? extends BioPAXElement> domain,
	                            Class range,
	                            boolean multipleCardinality)
	{
		super(property,
				getMethod,
				domain,
				range,
				multipleCardinality);
	}

// --------------------- GETTER / SETTER METHODS ---------------------

	public HashMap<Class, Set<Class>> getRestrictedRanges()
	{
		return restrictedRanges;
	}

// -------------------------- OTHER METHODS --------------------------

	public void addRangeRestriction(Class domain, Set<Class> ranges)
	{
		this.restrictedRanges.put(domain, ranges);
	}

	@Override
	protected void checkRestrictions(Object bean, Object value)
	{
		super.checkRestrictions(bean, value);
		Set<Class> classes = getRestrictedRangesFor(((BioPAXElement) bean).getModelInterface());
		if (classes != null && !isInstanceOfAtLeastOne(classes, value))
		{
			throw new IllegalBioPAXArgumentException(
					"The range restriction is violated \n" +
					value + "-->" + bean);
		}
	}

	public Set<Class> getRestrictedRangesFor(Class restrictedDomain)
	{
		Set<Class> classes = this.restrictedRanges.get(restrictedDomain);
		if (classes == null)
		{
			classes = new HashSet<Class>();
			classes.add(range);
		}
		return classes;
	}

	// --------------------- ACCESORS and MUTATORS---------------------
}
