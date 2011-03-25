package org.biopax.paxtools.model;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class BioPAXFactory
{
    private static Log log = LogFactory.getLog(BioPAXFactory.class);

    public BioPAXElement create(String localName, String uri)
    {
        return this.create((this.getLevel().getInterfaceForName(localName)), uri);
    }


    public <T extends BioPAXElement> T create(Class<T> aClass, String uri)
    {

        try {

            T bpe = InstantiateImplementingClass(aClass, uri);
            return bpe;

        } catch (InstantiationException e) {
            log.error("Could not instantiate " + aClass
                    + "Make sure that there is a default non-private noarg constructor");
            log.error(e.getStackTrace());
        } catch (IllegalAccessException e) {
            log.error("Could not instantiate a class implementing " + aClass
                    + " Make sure that there is a default non-private noarg constructor");
            log.error(e.getStackTrace());
        } catch (ClassNotFoundException e) {
            log.error("No implementing class for " + aClass);
            log.error(e.getStackTrace());
        }
        return null;
    }

    protected abstract <T extends BioPAXElement> T InstantiateImplementingClass(Class<T> aClass, String id)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException;

    public abstract boolean canInstantiate(Class<? extends BioPAXElement> aClass);

	public abstract Model createModel();

    public abstract BioPAXLevel getLevel();
}
