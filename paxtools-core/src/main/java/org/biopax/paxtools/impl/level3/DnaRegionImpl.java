package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.DnaRegion;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;


/**
 */
@Entity
@Indexed
class DnaRegionImpl extends NucleicAcidImpl implements DnaRegion
{
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------

    @Override @Transient
	public Class<? extends DnaRegion> getModelInterface()
	{
		return DnaRegion.class;
	}

}
