package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Degradation;


public class DegradationImpl extends ConversionImpl implements Degradation
{
	public DegradationImpl() {
	}

	public Class<? extends Degradation> getModelInterface()
    {
        return Degradation.class;
    }

}