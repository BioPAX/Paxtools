package org.biopax.paxtools.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXFactory;
import org.biopax.paxtools.model.Model;

import java.lang.reflect.Modifier;

public abstract class BioPAXFactoryAdaptor extends BioPAXFactory {
	private static final Log LOG = LogFactory.getLog(BioPAXFactoryAdaptor.class);
	
    protected void setId(BioPAXElement bpe, String uri)
    {
        ((BioPAXElementImpl) bpe).setRDFId(uri);
    }


    protected String mapClassName(Class<? extends BioPAXElement> aClass) 
    {
        String name = this.getClass().getPackage().getName() + "."
                + aClass.getSimpleName()
                + "Impl";
        return name;
    }

    @Override
    public boolean canInstantiate(Class<? extends BioPAXElement> aClass)    //TODO do better, check package etc..
    {
        try {
            String cname = mapClassName(aClass);
        	return !Modifier.isAbstract(Class.forName(cname).getModifiers());
        } catch (ClassNotFoundException e)
        {
            return false;
        } catch (Exception ex) {
        	LOG.error("Error in canInstantiate(" + aClass + ")", ex);
			return false;
		}
    }

    @Override
    public Model createModel() {
        return new ModelImpl(this);
    }

    
    @Override
	public <T extends BioPAXElement> Class<T> getImplClass(
			Class<T> aModelInterfaceClass) 
	{
		Class<T> implClass = null;

		if (aModelInterfaceClass.isInterface()) {
			String name = mapClassName(aModelInterfaceClass);
			try {
				implClass = (Class<T>) Class.forName(name);
			} catch (ClassNotFoundException e) {} //TODO log?
		}

		return implClass;
	}
}


