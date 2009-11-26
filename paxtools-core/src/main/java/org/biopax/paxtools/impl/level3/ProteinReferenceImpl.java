package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.ProteinReference;
import org.biopax.paxtools.model.BioPAXElement;

class ProteinReferenceImpl extends SequenceEntityReferenceImpl
        implements ProteinReference
{

    //
    // utilityClass interface implementation
    //
    ////////////////////////////////////////////////////////////////////////////

    public Class<? extends ProteinReference> getModelInterface()
    {
        return ProteinReference.class;
    }
}
