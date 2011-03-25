package org.biopax.paxtools.impl.level3;


import org.biopax.paxtools.impl.BioPAXFactoryAdaptor;
import org.biopax.paxtools.model.BioPAXLevel;

public class Level3FactoryImpl extends BioPAXFactoryAdaptor
{
    @Override
    public BioPAXLevel getLevel()
    {
        return BioPAXLevel.L3;
    }
}
