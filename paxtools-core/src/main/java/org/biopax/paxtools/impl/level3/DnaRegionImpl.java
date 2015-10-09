package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.DnaRegionReference;
import org.biopax.paxtools.model.level3.DnaRegion;
import org.biopax.paxtools.model.level3.EntityReference;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;


public class DnaRegionImpl extends NucleicAcidImpl implements DnaRegion
{
	public DnaRegionImpl() {
	}
	
// ------------------------ INTERFACE METHODS ------------------------

	@Override
	public void setEntityReference(EntityReference entityReference) {
		if(entityReference instanceof DnaRegionReference || entityReference == null)
			super.setEntityReference(entityReference);
		else
			throw new IllegalBioPAXArgumentException("setEntityReference failed: "
					+ entityReference.getUri() + " is not a DnaRegionReference.");
	}


// --------------------- Interface BioPAXElement ---------------------

    @Override
	public Class<? extends DnaRegion> getModelInterface()
	{
		return DnaRegion.class;
	}

}
