package org.biopax.paxtools.impl.level2;


import org.biopax.paxtools.impl.BioPAXFactoryAdaptor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;

public class Level2FactoryImpl extends BioPAXFactoryAdaptor {

    @Override
    public <T extends BioPAXElement> T InstantiateImplementingClass(Class<T> aClass, String id)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        T bpe = (T) Class.forName(mapClassName(aClass)).newInstance();
        if(bpe!=null) setId(bpe,id);
        return bpe;
    }

    @Override
    public BioPAXLevel getLevel()
    {
        return BioPAXLevel.L2;
    }


}
