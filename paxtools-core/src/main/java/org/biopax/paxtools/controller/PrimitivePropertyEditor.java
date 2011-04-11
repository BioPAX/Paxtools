package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.lang.reflect.Method;

/**
 * Provides a primitive (int, float, double)  class compatible editor by extending the {@link
 * org.biopax.paxtools.controller.PropertyEditor}.
 *
 * @see org.biopax.paxtools.controller.PropertyEditor
 */
public class PrimitivePropertyEditor<D extends BioPAXElement, R>
		extends PropertyEditor<D, R>
{
// ------------------------------ FIELDS ------------------------------

	private Object unknownValue = null;

// --------------------------- CONSTRUCTORS ---------------------------

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
			unknownValue = BioPAXElement.UNKNOWN_DOUBLE;
		}
		else if (range.equals(float.class))
		{
			unknownValue = BioPAXElement.UNKNOWN_FLOAT;
		}
		else if (range.equals(int.class))
		{
			unknownValue = BioPAXElement.UNKNOWN_INT;
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
	 * According the editor type, this methods checks if value equals to one of the unknown values
	 * defined under {@link org.biopax.paxtools.model.BioPAXElement}.
	 *
	 * @param value the value to be checked if it is unknown
	 * @return true, if value equals to the predefined unknown value
	 */
	@Override
	public boolean isUnknown(Object value)
	{
		return value == unknownValue || value.equals(unknownValue);
	}

	
	@Override
	public Object getUnknown() {
		return unknownValue;
	}
}
