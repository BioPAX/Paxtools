package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.transport;

/**
 */
class transportImpl extends conversionImpl implements transport
{
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------

	public Class<? extends BioPAXElement> getModelInterface()
	{
		return transport.class;
	}
}
