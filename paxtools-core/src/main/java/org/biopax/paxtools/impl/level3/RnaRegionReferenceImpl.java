package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.RnaRegionReference;
import org.biopax.paxtools.model.level3.SequenceLocation;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class RnaRegionReferenceImpl extends NucleicAcidRegionReferenceImpl implements RnaRegionReference
{
	//
	// utilityClass interface implementation
	//
	////////////////////////////////////////////////////////////////////////////
    RnaRegionReference rnaRegionReference;
    SequenceLocation sequenceLocation;

    public RnaRegionReferenceImpl() {
	}

    @Override @Transient
	public Class<? extends RnaRegionReference> getModelInterface()
	{
		return RnaRegionReference.class;
	}

}


