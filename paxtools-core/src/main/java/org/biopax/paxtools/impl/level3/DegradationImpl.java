package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Degradation;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
class DegradationImpl extends ConversionImpl implements Degradation
{
    @Transient
	public Class<? extends Degradation> getModelInterface()
    {
        return Degradation.class;
    }

}
