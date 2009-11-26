package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;

import java.lang.reflect.Method;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

/**
 * Provides a primitive (int, float, double)  class compatible editor
 * by extending the {@link org.biopax.paxtools.controller.PropertyEditor}.
 *
 * @see org.biopax.paxtools.controller.PropertyEditor
 */
public class PrimitivePropertyEditor extends PropertyEditor
{
// ------------------------------ FIELDS ------------------------------

	private Object unknownValue = null;

// --------------------------- CONSTRUCTORS ---------------------------

	public PrimitivePropertyEditor(String property, Method getMethod,
	                               Class<? extends BioPAXElement> domain,
                                   Class range,
	                               boolean multipleCardinality)
	{
		super(property,
			getMethod,
			domain,
			range,
			multipleCardinality);

		assert(this.range == Boolean.class || this.range.isPrimitive());

		if (this.range.equals(double.class))
		{
			unknownValue = BioPAXElement.UNKNOWN_DOUBLE;
		}
		else if (this.range.equals(float.class))
		{
			unknownValue = BioPAXElement.UNKNOWN_FLOAT;
		}
		else if (this.range.equals(int.class))
		{
			unknownValue = BioPAXElement.UNKNOWN_INT;
		}

	}

// -------------------------- OTHER METHODS --------------------------

    @Override
	protected void invokeMethod(Method toInvoke, Object bean, Object o)
	{
		if (o.getClass().equals(range))
		{
			super.invokeMethod(toInvoke, bean, o);
		}
		else if (o.getClass().equals(String.class))
		{
			String s = (String) o;
			try
			{
				if (this.range.equals(double.class))
				{
					super.invokeMethod(toInvoke, bean, Double.valueOf(s));
				}
				else if (this.range.equals(float.class))
				{
					super.invokeMethod(toInvoke, bean, Float.valueOf(s));
				}
				else if (this.range.equals(int.class))
				{
					super.invokeMethod(toInvoke, bean, Integer.valueOf(s));
				}
                else if (this.range.equals(Boolean.class))
				{
					super.invokeMethod(toInvoke, bean, Boolean.valueOf(s));
				}
			}
			catch (NumberFormatException e)
			{
				throw new IllegalBioPAXArgumentException(
                    "Failed to convert literal " + s +
					" to " + range.getSimpleName()
                    + " for " + property, e);
			}
		}
        else
        {
            throw new IllegalBioPAXArgumentException();
        }
	}

    /**
     * According the editor type, this methods checks if
     * value equals to one of the unknown values defined under
     * {@link org.biopax.paxtools.model.BioPAXElement}.
     *
     * @param value the value to be checked if it is unknown
     * @return true, if value equals to the predefined unknown value
     */
    @Override
    public boolean isUnknown(Object value)
	{
		return value == unknownValue || value.equals(unknownValue);
	}
}
