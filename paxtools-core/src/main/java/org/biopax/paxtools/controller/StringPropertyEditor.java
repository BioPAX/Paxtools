package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;

import java.lang.reflect.Method;

/**
 * Provides an String class compatible editor by extending the {@link
 * PropertyEditor}.
 *
 * @see PropertyEditor
 */
public class StringPropertyEditor<D extends BioPAXElement> extends AbstractPropertyEditor<D, String>
		implements DataPropertyEditor<D,String>
{
// --------------------------- CONSTRUCTORS ---------------------------

	public StringPropertyEditor(String property, Method getMethod,
	                            Class<D> domain,
	                            boolean multipleCardinality)
	{
		super(property,
				getMethod,
				domain,
				String.class,
				multipleCardinality);
	}

	@Override
	protected String parseValueFromString(String value)
	{
		return value;
	}
}
