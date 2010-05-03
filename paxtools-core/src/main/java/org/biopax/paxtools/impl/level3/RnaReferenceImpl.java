package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.RnaReference;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@Indexed
class RnaReferenceImpl extends NucleicAcidReferenceImpl 
	implements RnaReference
{

	//
	// utilityClass interface implementation
	//
	////////////////////////////////////////////////////////////////////////////
    @Transient
	public Class<? extends RnaReference> getModelInterface()
	{
		return RnaReference.class;
	}
}
