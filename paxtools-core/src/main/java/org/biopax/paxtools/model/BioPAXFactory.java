package org.biopax.paxtools.model;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.reflect.Modifier;

public abstract class BioPAXFactory
{
    private static Log log = LogFactory.getLog(BioPAXFactory.class);

    public BioPAXElement create(String localName, String uri)
    {
        return this.create((this.getLevel().getInterfaceForName(localName)), uri);
    }


    public <T extends BioPAXElement> T create(Class<T> aClass, String uri)
    {
        T bpe = null;
        try {
            bpe = getImplementingClass(aClass).newInstance();
        } catch (InstantiationException e)
        {
            log.error("Could not instantiate "+ aClass + "with "+ bpe.getClass());
            log.error("Make sure that there is a default non-private noarg constructor");
            log.error(e.getStackTrace());
        } catch (IllegalAccessException e) {
            log.error("Could not instantiate "+ aClass + "with "+ bpe.getClass());
            log.error("Make sure that there is a default non-private noarg constructor");
            log.error(e.getStackTrace());
        }
        setId(bpe, uri);
        return bpe;
	}

    protected abstract void setId(BioPAXElement bpe, String uri);

    public abstract <T extends BioPAXElement> Class<? extends T>  getImplementingClass(Class<T> aClass);

    public boolean canInstantiate(Class<? extends BioPAXElement> aClass)
    {
        return !Modifier.isAbstract(getImplementingClass(aClass).getModifiers());
    }

	public abstract Model createModel();

    public abstract BioPAXLevel getLevel();
}
