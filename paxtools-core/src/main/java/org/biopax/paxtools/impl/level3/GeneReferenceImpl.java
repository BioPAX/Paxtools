package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.GeneReference;
import org.biopax.paxtools.model.BioPAXElement;

class GeneReferenceImpl extends EntityReferenceImpl
        implements GeneReference
{
    /**
     * Constructor.
     */
    public GeneReferenceImpl()
    {
    }

    //
    // utilityClass interface implementation
    //
    ////////////////////////////////////////////////////////////////////////////

    public Class<? extends GeneReference> getModelInterface()
    {
        return GeneReference.class;
    }
}
