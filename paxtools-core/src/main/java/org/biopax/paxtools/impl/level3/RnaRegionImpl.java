package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.RnaRegion;


public class RnaRegionImpl extends NucleicAcidImpl implements RnaRegion
{
	public RnaRegionImpl() {
	}
	
// --------------------- Interface BioPAXElement ---------------------

    @Override
	public Class<? extends RnaRegion> getModelInterface()
	{
		return RnaRegion.class;
	}

}
