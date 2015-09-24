package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.RnaReference;


public class RnaReferenceImpl extends NucleicAcidReferenceImpl
	implements RnaReference
{

	public RnaReferenceImpl() {
	}

	//
	// utilityClass interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	public Class<? extends RnaReference> getModelInterface()
	{
		return RnaReference.class;
	}
}
