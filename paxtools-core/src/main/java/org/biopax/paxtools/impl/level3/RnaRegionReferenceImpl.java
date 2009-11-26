package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.RnaRegionReference;
import org.biopax.paxtools.model.level3.SequenceLocation;

class RnaRegionReferenceImpl extends SequenceEntityReferenceImpl implements RnaRegionReference
{
	//
	// utilityClass interface implementation
	//
	////////////////////////////////////////////////////////////////////////////
    RnaRegionReference rnaRegionReference;
    SequenceLocation sequenceLocation;


    @Override
	public Class<? extends RnaRegionReference> getModelInterface()
	{
		return RnaRegionReference.class;
	}

    public RnaRegionReference getSubRegion() {
        return rnaRegionReference;
    }

    public void setSubRegion(RnaRegionReference rnaRegionReference) {
        this.rnaRegionReference = rnaRegionReference;
    }

	public SequenceLocation getAbsoluteRegion() {
		return sequenceLocation;
	}

	public void setAbsoluteRegion(SequenceLocation sequenceLocation) {
		this.sequenceLocation = sequenceLocation;
	}
}


