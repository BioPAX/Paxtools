package org.biopax.paxtools.controller;

import org.biopax.paxtools.util.Filter;

/**
 * A generic interface for bidirectional property filter
 */
public interface PropertyFilterBilinked extends Filter<PropertyEditor>
{
	 boolean filterInverse(PropertyEditor editor);
}