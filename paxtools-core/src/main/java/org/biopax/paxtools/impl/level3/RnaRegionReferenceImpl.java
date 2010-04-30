package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.RnaRegionReference;
import org.biopax.paxtools.model.level3.SequenceLocation;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
class RnaRegionReferenceImpl extends NucleicAcidRegionReferenceImpl<RnaRegionReference> implements RnaRegionReference
{
	//
	// utilityClass interface implementation
	//
	////////////////////////////////////////////////////////////////////////////
    RnaRegionReference rnaRegionReference;
    SequenceLocation sequenceLocation;


    @Override @Transient
	public Class<? extends RnaRegionReference> getModelInterface()
	{
		return RnaRegionReference.class;
	}

}


