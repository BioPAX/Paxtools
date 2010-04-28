package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.DnaRegionReference;
import org.biopax.paxtools.model.level3.SequenceLocation;

class DnaRegionReferenceImpl extends NucleicAcidRegionReferenceImpl<DnaRegionReference>
		implements DnaRegionReference
{
	DnaRegionReferenceImpl()
	{
	}
	

	@Override
	public Class<? extends DnaRegionReference> getModelInterface()
	{
		return DnaRegionReference.class;
	}


}


