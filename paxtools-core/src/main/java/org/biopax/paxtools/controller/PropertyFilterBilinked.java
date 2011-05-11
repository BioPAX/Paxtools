package org.biopax.paxtools.controller;

import org.biopax.paxtools.util.Filter;

public interface PropertyFilterBilinked extends Filter<PropertyEditor>
{
	 boolean filterInverse(PropertyEditor editor);
}