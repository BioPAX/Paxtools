package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

/**
 * Provides an ENUM class compatible editor by extending the
 *  {@link org.biopax.paxtools.controller.PropertyEditor}.
 *
 * @see org.biopax.paxtools.controller.PropertyEditor
 */
public class EnumeratedPropertyEditor extends PropertyEditor
{
// --------------------------- CONSTRUCTORS ---------------------------

    public EnumeratedPropertyEditor(String property, Method getMethod,
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
            String value = (String) o;
            value = value.replaceAll("-", "_");
            try
            {
                toInvoke
                        .invoke(domain.cast(bean),
                                Enum.valueOf(range, value));
            }
            catch (IllegalAccessException e) {
                throw new IllegalBioPAXArgumentException("Failed to convert literal " +
                        value + " to enum for " + property);
            } catch (InvocationTargetException e) {
                throw new IllegalBioPAXArgumentException("Failed to convert literal " +
                        value + " to enum for " + property);
            }
        }
    }
}

