package org.biopax.paxtools.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Interface for demarcating excise-able boundaries of the object graph.
 * @author Ozgun Babur
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoComplete
{
	boolean forward() default true;
	boolean backward() default false;
}
