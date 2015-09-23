package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.DnaRegion;


public class DnaRegionImpl extends NucleicAcidImpl implements DnaRegion
{
	public DnaRegionImpl() {
	}
	
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------

    @Override
	public Class<? extends DnaRegion> getModelInterface()
	{
		return DnaRegion.class;
	}

}
