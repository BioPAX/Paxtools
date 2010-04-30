package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.RnaRegion;

import javax.persistence.Entity;
import javax.persistence.Transient;


/**
 */
@Entity
class RnaRegionImpl extends NucleicAcidImpl implements RnaRegion
{
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------

    @Override @Transient
	public Class<? extends RnaRegion> getModelInterface()
	{
		return RnaRegion.class;
	}


}
