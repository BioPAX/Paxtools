package org.biopax.paxtools.impl.level3;


import org.biopax.paxtools.impl.BioPAXFactoryAdaptor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;

public class Level3FactoryImpl extends BioPAXFactoryAdaptor {
    @Override
    public BioPAXLevel getLevel() {
        return BioPAXLevel.L3;
    }

    @Override
    protected <T extends BioPAXElement> T createInstance(Class<T> aClass, String id)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        T bpe = (T) Class.forName(mapClassName(aClass)).newInstance();
        if(bpe!=null) setId(bpe,id);
        return bpe;
    }

}
