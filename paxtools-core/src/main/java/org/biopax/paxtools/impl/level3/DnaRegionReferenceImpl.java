package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.DnaRegionReference;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@Indexed
class DnaRegionReferenceImpl extends NucleicAcidRegionReferenceImpl
		implements DnaRegionReference
{

	@Override @Transient
	public Class<? extends DnaRegionReference> getModelInterface()
	{
		return DnaRegionReference.class;
	}


}


