package org.biopax.paxtools.model.level3;

/**
 * A RNA region reference
 */

public interface RnaRegionReference extends SequenceEntityReference
{
    RnaRegionReference getSubRegion();

    void setSubRegion(RnaRegionReference dnaRegionReference);
    
    SequenceLocation getAbsoluteRegion();
    
    void setAbsoluteRegion(SequenceLocation sequenceLocation);
}
