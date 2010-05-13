package org.biopax.paxtools.util;

/**
 * @author Ozgun Babur
 */
public @interface AutoComplete
{
	boolean forward() default true;
	boolean backward() default false;
}
