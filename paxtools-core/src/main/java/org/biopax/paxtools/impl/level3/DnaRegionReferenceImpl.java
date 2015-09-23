package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.DnaRegionReference;


public class DnaRegionReferenceImpl extends NucleicAcidRegionReferenceImpl implements DnaRegionReference
{
	public DnaRegionReferenceImpl()
	{
	}

	@Override
	public Class<? extends DnaRegionReference> getModelInterface()
	{
		return DnaRegionReference.class;
	}
}
