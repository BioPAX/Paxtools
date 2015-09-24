package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.ProteinReference;


public class ProteinReferenceImpl extends SequenceEntityReferenceImpl
        implements ProteinReference
{
	public ProteinReferenceImpl() {
	}

    public Class<? extends ProteinReference> getModelInterface()
    {
        return ProteinReference.class;
    }
}
