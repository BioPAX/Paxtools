package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Transport;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@Indexed
class TransportImpl extends ConversionImpl implements Transport
{
// --------------------- Interface BioPAXElement ---------------------

	@Transient
    public Class<? extends Transport> getModelInterface()
	{
		return Transport.class;
	}
}
