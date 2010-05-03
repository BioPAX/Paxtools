package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.RnaRegionReference;
import org.biopax.paxtools.model.level3.SequenceLocation;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@Indexed
class RnaRegionReferenceImpl extends NucleicAcidRegionReferenceImpl implements RnaRegionReference
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


