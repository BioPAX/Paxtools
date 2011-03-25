package org.biopax.paxtools.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.Model;

public abstract class BioPAXFactoryAdaptor extends BioPAXFactory {
    private static Log log = LogFactory.getLog(BioPAXFactoryAdaptor.class);

    @Override
    protected void setId(BioPAXElement bpe, String uri) {
        ((BioPAXElementImpl) bpe).setRDFId(uri);
    }

    @Override
    public <T extends BioPAXElement> Class<? extends T> getImplementingClass(Class<T> aClass) {
        String name = aClass.getSimpleName();
        name = this.getClass().getPackage().getName() + "."
        	+ name
        	+ "Impl";
        try {
            return (Class<? extends T>) Class.forName(name);
        } catch (ClassNotFoundException e) {
            log.error("No class for " + name);
            log.error(e.getStackTrace());
            return null;
        }
    }

    @Override
    public Model createModel() {
        return new ModelImpl(this);
    }
}


