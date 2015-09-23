package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.DnaReference;


public class DnaReferenceImpl extends NucleicAcidReferenceImpl implements
		DnaReference
{
	public DnaReferenceImpl() {
	}

	@Override
	public Class<? extends DnaReference> getModelInterface()
	{                                        
		return DnaReference.class;
	}

}

