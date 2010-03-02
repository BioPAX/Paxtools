package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.UnificationXref;

public class UnificationXrefImpl extends XrefImpl implements UnificationXref {

    //
    // BioPAXElement interface implementation
    //
    ////////////////////////////////////////////////////////////////////////////
    public Class<? extends UnificationXref> getModelInterface() {
        return UnificationXref.class;
    }
}
