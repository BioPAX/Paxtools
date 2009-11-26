package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Transport;
import org.biopax.paxtools.model.BioPAXElement;

/**
 */
class TransportImpl extends ConversionImpl implements Transport
{
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------

	public Class<? extends Transport> getModelInterface()
	{
		return Transport.class;
	}
}
