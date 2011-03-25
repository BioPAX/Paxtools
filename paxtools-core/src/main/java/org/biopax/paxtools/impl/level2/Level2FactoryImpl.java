package org.biopax.paxtools.impl.level2;


import org.biopax.paxtools.impl.BioPAXFactoryAdaptor;
import org.biopax.paxtools.model.BioPAXLevel;

public class Level2FactoryImpl extends BioPAXFactoryAdaptor
{

    @Override
    public BioPAXLevel getLevel()
    {
        return BioPAXLevel.L2;
    }
}
