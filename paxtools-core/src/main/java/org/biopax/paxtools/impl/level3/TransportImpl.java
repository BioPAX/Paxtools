package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.Transport;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@Indexed(index=BioPAXElementImpl.SEARCH_INDEX_FOR_ENTITY)
class TransportImpl extends ConversionImpl implements Transport
{
// --------------------- Interface BioPAXElement ---------------------

	@Transient
    public Class<? extends Transport> getModelInterface()
	{
		return Transport.class;
	}
}
