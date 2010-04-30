package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.RnaReference;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
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
