package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.SequenceInterval;
import org.biopax.paxtools.model.level3.SequenceRegionVocabulary;

/**
 * TODO:Class description
 * User: demir
 * Date: Apr 17, 2009
 * Time: 7:55:39 PM
 */
abstract class SequenceRegionReferenceImpl extends SequenceEntityReferenceImpl
{
    private SequenceInterval subRegion;
    private SequenceRegionVocabulary regionType;


    public SequenceInterval getSubRegion() {
        return subRegion;
    }

    public void setSubRegion(SequenceInterval subRegion) {
        this.subRegion = subRegion;
    }

    public SequenceRegionVocabulary getRegionType() {
        return regionType;
    }

    public void setRegionType(SequenceRegionVocabulary regionType) {
        this.regionType = regionType;
    }
}
