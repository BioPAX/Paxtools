package org.biopax.paxtools.util;

import org.biopax.paxtools.model.BioPAXElement;

/**
 * User: demir Date: Oct 9, 2007 Time: 2:55:24 PM
 */
public class BidirectionalLinkViolationException
	extends IllegalBioPAXArgumentException
{
	public BidirectionalLinkViolationException(BioPAXElement source,
	                                           BioPAXElement target)
	{
		super("Bidirectional link was violated between " + source.getRDFId() +
			" and " + target.getRDFId());
	}
}
