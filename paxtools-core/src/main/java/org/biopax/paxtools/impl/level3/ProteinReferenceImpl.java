package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.ProteinReference;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
class ProteinReferenceImpl extends SequenceEntityReferenceImpl
        implements ProteinReference
{

    @Transient
    public Class<? extends ProteinReference> getModelInterface()
    {
        return ProteinReference.class;
    }
}
