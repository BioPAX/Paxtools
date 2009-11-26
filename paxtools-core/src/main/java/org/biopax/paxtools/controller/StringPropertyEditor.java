package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;

import java.lang.reflect.Method;

/**
 * Provides an String class compatible editor by extending the
 *  {@link org.biopax.paxtools.controller.PropertyEditor}.
 *
 * @see org.biopax.paxtools.controller.PropertyEditor
 */
public class StringPropertyEditor extends PropertyEditor
{
// --------------------------- CONSTRUCTORS ---------------------------

	public StringPropertyEditor(String property, Method getMethod,
	                            Class<? extends BioPAXElement> domain,
                                Class range,
	                            boolean multipleCardinality)
	{
		super(property,
			getMethod,
			domain,
			range,
			multipleCardinality);
		assert(range.equals(String.class));
	}
}
