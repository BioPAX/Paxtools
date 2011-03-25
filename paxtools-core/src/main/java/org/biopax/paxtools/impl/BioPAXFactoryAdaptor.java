package org.biopax.paxtools.impl;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.Model;

import java.lang.reflect.Modifier;

public abstract class BioPAXFactoryAdaptor extends BioPAXFactory {


    protected void setId(BioPAXElement bpe, String uri)
    {
        ((BioPAXElementImpl) bpe).setRDFId(uri);
    }


    protected String mapClassName(Class<? extends BioPAXElement> aClass) {
        String name = aClass.getSimpleName();
        name = this.getClass().getPackage().getName() + "."
                + name
                + "Impl";
        return name;
    }

    @Override
    public boolean canInstantiate(Class<? extends BioPAXElement> aClass)
    {
        try {
            return !Modifier.isAbstract(Class.forName(mapClassName(aClass)).getModifiers());
        } catch (ClassNotFoundException e)
        {
            return false;
        }
    }

    @Override
    public Model createModel() {
        return new ModelImpl(this);
    }
}


