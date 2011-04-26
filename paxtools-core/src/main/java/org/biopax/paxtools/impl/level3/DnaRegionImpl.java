package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.DnaRegion;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;


@Entity
@Indexed(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
public class DnaRegionImpl extends NucleicAcidImpl implements DnaRegion
{
	public DnaRegionImpl() {
	}
	
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------

    @Override @Transient
	public Class<? extends DnaRegion> getModelInterface()
	{
		return DnaRegion.class;
	}

}
