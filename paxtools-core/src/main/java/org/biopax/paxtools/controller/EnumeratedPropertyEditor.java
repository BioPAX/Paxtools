package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;

import java.lang.reflect.Method;

/**
 * Provides an ENUM class compatible editor by extending the {@link org.biopax.paxtools.controller.PropertyEditor}.
 *
 * @see org.biopax.paxtools.controller.PropertyEditor
 */
public class EnumeratedPropertyEditor<D extends BioPAXElement, R extends Enum>
		extends PropertyEditor<D, R>
{
// --------------------------- CONSTRUCTORS ---------------------------

	public EnumeratedPropertyEditor(String property, Method getMethod,
	                                Class<D> domain,
	                                Class<R> range,
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
	protected R parseValueFromString(String value)
	{

		value = value.replaceAll("-", "_");
		return (R) Enum.valueOf(this.getRange(), value);
	}
}

