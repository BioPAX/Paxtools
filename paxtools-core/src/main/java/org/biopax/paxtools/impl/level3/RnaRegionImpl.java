package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.RnaRegion;


/**
 */
class RnaRegionImpl extends SimplePhysicalEntityImpl implements RnaRegion
{
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------

    @Override
	public Class<? extends RnaRegion> getModelInterface()
	{
		return RnaRegion.class;
	}


}
