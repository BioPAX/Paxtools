package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.DnaRegion;

import javax.persistence.Transient;


/**
 */
class DnaRegionImpl extends SimplePhysicalEntityImpl implements DnaRegion
{
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------

    @Override @Transient
	public Class<? extends DnaRegion> getModelInterface()
	{
		return DnaRegion.class;
	}

}
