package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.DnaReference;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;

@Entity
@Indexed
class DnaReferenceImpl extends NucleicAcidReferenceImpl implements
		DnaReference
{

	@Override @Transient
	public Class<? extends DnaReference> getModelInterface()
	{                                        
		return DnaReference.class;
	}

}

