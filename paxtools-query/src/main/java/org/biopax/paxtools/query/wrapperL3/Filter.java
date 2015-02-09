package org.biopax.paxtools.query.wrapperL3;

import org.biopax.paxtools.model.level3.Level3Element;

/**
 * This is the base filter class that can be applied to any level 3 element. The query will traverse
 * only the objects that this filter lets.
 *
 * @author Ozgun Babur
 */
public abstract class Filter
{
	/**
	 * Checks if it is ok to traverse the given level 3 element.
	 * @param ele level 3 element to check
	 * @return true if ok to traverse
	 */
	public abstract boolean okToTraverse(Level3Element ele);
}
