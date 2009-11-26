package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.UnificationXref;
import org.biopax.paxtools.model.BioPAXElement;

public class UnificationXrefImpl extends XrefImpl implements UnificationXref {

    //
    // BioPAXElement interface implementation
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public Class<? extends UnificationXref> getModelInterface() {
        return UnificationXref.class;
    }
}
