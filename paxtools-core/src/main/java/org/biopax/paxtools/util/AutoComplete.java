package org.biopax.paxtools.util;

/**
 * Interface for demarcating excise-able boundaries of the object graph.
 * @author Ozgun Babur
 */
public @interface AutoComplete
{
	boolean forward() default true;
	boolean backward() default false;
}
