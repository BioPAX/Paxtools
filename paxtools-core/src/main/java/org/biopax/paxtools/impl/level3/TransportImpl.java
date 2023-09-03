package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Transport;


public class TransportImpl extends ConversionImpl implements Transport
{
	public TransportImpl() {
	}
	
	// --------------------- Interface BioPAXElement ---------------------

  public Class<? extends Transport> getModelInterface()
	{
		return Transport.class;
	}
}
