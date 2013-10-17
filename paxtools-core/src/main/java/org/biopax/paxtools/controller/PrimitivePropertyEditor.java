package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;

/**
 * Provides a primitive (int, float, double)  class compatible editor by extending the {@link
 * PropertyEditor}.
 *
 * @see PropertyEditor
 */
public class PrimitivePropertyEditor<D extends BioPAXElement, R>
		extends AbstractPropertyEditor<D, R>
{
// ------------------------------ FIELDS ------------------------------

	/**
	 * When a value is not present in the OWL file
	 */
	private R unknownValue = null;

// --------------------------- CONSTRUCTORS ---------------------------

	/**
	 *
	 * @param property Name of the property, e.g. stoichiometricCoefficient.
	 * @param getMethod A "Method" class that represents the getter method. e.g. getEntityReference()
	 * @param domain name of the domain of this property. e.g. PhysicalEntity
	 * @param range name of the range of this property. e.g. EntityReference.
	 * @param multipleCardinality false if this property is functional, e.g. many-to-one or one-to-one.
	 */
	public PrimitivePropertyEditor(String property, Method getMethod,
	                               Class<D> domain,
	                               Class<R> range,
	                               boolean multipleCardinality)
	{
		super(property,
				getMethod,
				domain,
				range,
				multipleCardinality);

		assert (range == Boolean.class || range.isPrimitive());

		if (range.equals(double.class))
		{
			unknownValue = ((R) BioPAXElement.UNKNOWN_DOUBLE);
		}
		else if (range.equals(float.class))
		{
			unknownValue = ((R) BioPAXElement.UNKNOWN_FLOAT);
		}
		else if (range.equals(int.class))
		{
			unknownValue = ((R) BioPAXElement.UNKNOWN_INT);
		}

	}

// -------------------------- OTHER METHODS --------------------------


	@Override
	protected R parseValueFromString(String s)
	{
		Class<R> range = this.getRange();
		R value = null;
		try
		{
			if (range.equals(double.class))
			{
				value= (R) Double.valueOf(s);
			}
			else if (range.equals(float.class))
			{
				value=  (R) Float.valueOf(s);
			}
			else if (range.equals(int.class))
			{
				value= (R) Integer.valueOf(s);
			}
			else if (range.equals(Boolean.class))
			{
				value= (R) Boolean.valueOf(s);
			}
		}

		catch (NumberFormatException e)

		{
			throw new IllegalBioPAXArgumentException(
					"Failed to convert literal " + s +
					" to " + range.getSimpleName()
					+ " for " + property, e);
		}
		return value;
	}


	/**
	 * According the editor type, this methods checks if value equals 
	 * to one of the unknown values defined under {@link org.biopax.paxtools.model.BioPAXElement}
	 *  or is an empty set or set of "unknown" values.
	 *
	 * @param value the value to be checked if it is unknown
	 * @return true, if value equals to the predefined unknown value
	 */
	@Override
	public boolean isUnknown(Object value)
	{
		return (value instanceof Set) 
			? emptySetOrContainsOnlyUnknowns((Set)value) 
				: emptySetOrContainsOnlyUnknowns(Collections.singleton(value));
	}

	
	private boolean emptySetOrContainsOnlyUnknowns(Set value) {
		for(Object o : value) {
			if(o != unknownValue && !o.equals(unknownValue)) {
				return false; // found a "known" value
			}
		}
		// empty set or all unknown
		return true;
	}

	
	@Override
	public R getUnknown() {
		return unknownValue;
	}
}
