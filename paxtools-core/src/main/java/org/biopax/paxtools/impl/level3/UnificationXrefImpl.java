package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.UnificationXref;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@Indexed
public class UnificationXrefImpl extends XrefImpl implements UnificationXref {

    @Transient
    public Class<? extends UnificationXref> getModelInterface() {
        return UnificationXref.class;
    }
}
