package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Transport;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
class TransportImpl extends ConversionImpl implements Transport
{
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------

	@Transient
    public Class<? extends Transport> getModelInterface()
	{
		return Transport.class;
	}
}
