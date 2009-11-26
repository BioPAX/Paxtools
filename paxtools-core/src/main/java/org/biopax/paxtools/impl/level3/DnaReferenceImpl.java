package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.DnaReference;
import org.biopax.paxtools.model.level3.SequenceInterval;
import org.biopax.paxtools.model.BioPAXElement;

class DnaReferenceImpl extends SequenceEntityReferenceImpl implements
                                                                  DnaReference
{
	private SequenceInterval genomicRegion;

	//
	// utilityClass interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

    @Override
	public Class<? extends DnaReference> getModelInterface()
	{
		return DnaReference.class;
	}

	public SequenceInterval getGenomicRegion()
	{
		return genomicRegion;
	}

	public void setGenomicRegion(SequenceInterval genomicRegion)
	{
		this.genomicRegion = genomicRegion;
	}
}


