package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.DnaRegionReference;
import org.biopax.paxtools.model.level3.SequenceLocation;

import javax.persistence.Transient;

class DnaRegionReferenceImpl extends NucleicAcidRegionReferenceImpl
		implements DnaRegionReference
{

	@Override @Transient
	public Class<? extends DnaRegionReference> getModelInterface()
	{
		return DnaRegionReference.class;
	}


}


