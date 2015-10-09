package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.model.level3.RnaRegionReference;
import org.biopax.paxtools.model.level3.RnaRegion;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;


public class RnaRegionImpl extends NucleicAcidImpl implements RnaRegion
{
	public RnaRegionImpl() {
	}


	@Override
	public void setEntityReference(EntityReference entityReference) {
		if(entityReference instanceof RnaRegionReference || entityReference == null)
			super.setEntityReference(entityReference);
		else
			throw new IllegalBioPAXArgumentException("setEntityReference failed: "
					+ entityReference.getUri() + " is not a RnaRegionReference.");
	}
// --------------------- Interface BioPAXElement ---------------------

    @Override
	public Class<? extends RnaRegion> getModelInterface()
	{
		return RnaRegion.class;
	}

}
