package org.biopax.paxtools.model.level3;

import java.util.Set;

/**
 * A DNA region reference
 */

public interface DnaRegionReference extends SequenceEntityReference
{
    DnaRegionReference getSubRegion();

    void setSubRegion(DnaRegionReference dnaRegionReference);
    
    SequenceLocation getAbsoluteRegion();
    
    void setAbsoluteRegion(SequenceLocation sequenceLocation);



//	DnaRegionReference getRegionOf();
//
//	void setRegionOf(DnaRegionReference dnaRegionReference);
//
//		// Property Initiation Region
//
//	public Set<NucleicAcid> getInitiationRegion();
//
//	void addInitiationRegion(NucleicAcid initiationRegion);
//
//	void removeInitiationRegion(NucleicAcid initiationRegion);
//
//	public void setInitiationRegion(Set<NucleicAcid> initiationRegion);
}
