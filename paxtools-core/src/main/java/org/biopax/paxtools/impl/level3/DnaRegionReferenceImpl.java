package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.DnaRegionReference;
import org.biopax.paxtools.model.level3.SequenceLocation;

class DnaRegionReferenceImpl extends SequenceEntityReferenceImpl implements DnaRegionReference
{
	//
	// utilityClass interface implementation
	//
	////////////////////////////////////////////////////////////////////////////
    DnaRegionReference dnaRegionReference;
    SequenceLocation sequenceLocation;

    @Override
	public Class<? extends DnaRegionReference> getModelInterface()
	{
		return DnaRegionReference.class;
	}

    public DnaRegionReference getSubRegion() {
        return dnaRegionReference;
    }

    public void setSubRegion(DnaRegionReference dnaRegionReference) {
        this.dnaRegionReference = dnaRegionReference;
    }

	public SequenceLocation getAbsoluteRegion() {
		return sequenceLocation;
	}

	public void setAbsoluteRegion(SequenceLocation sequenceLocation) {
		this.sequenceLocation = sequenceLocation;
	}
    
}


