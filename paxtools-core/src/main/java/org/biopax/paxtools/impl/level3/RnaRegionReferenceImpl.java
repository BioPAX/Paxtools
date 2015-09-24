package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.RnaRegionReference;


public class RnaRegionReferenceImpl extends NucleicAcidRegionReferenceImpl implements RnaRegionReference
{
	//
	// utilityClass interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

    public RnaRegionReferenceImpl() {
	}

    @Override
	public Class<? extends RnaRegionReference> getModelInterface()
	{
		return RnaRegionReference.class;
	}

}


