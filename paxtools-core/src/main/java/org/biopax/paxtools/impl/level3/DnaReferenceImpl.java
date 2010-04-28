package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.DnaReference;
import org.biopax.paxtools.model.level3.SequenceInterval;
import org.biopax.paxtools.model.level3.SequenceRegionVocabulary;
import org.biopax.paxtools.model.level3.DnaRegionReference;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
class DnaReferenceImpl extends NucleicAcidReferenceImpl implements
		DnaReference
{
	
	DnaReferenceImpl()
	{
	}


	@Override @Transient
	public Class<? extends DnaReference> getModelInterface()
	{                                        
		return DnaReference.class;
	}


}

