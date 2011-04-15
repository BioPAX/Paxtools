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

            T bpe = createInstance(aClass, uri);
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

    protected abstract <T extends BioPAXElement> T createInstance(Class<T> aClass, String id)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException;

    public abstract boolean canInstantiate(Class<? extends BioPAXElement> aClass);

	public abstract Model createModel();

    public abstract BioPAXLevel getLevel();
    
    /**
     * Get a concrete or abstract BioPAX type (not interface), 
     * from org.biopax.paxtools.impl..*, i.e., one that has 
     * persistence/search annotations, etc. This may be required for
     * some DAO and web service controllers; it also returns such 
     * abstract BioPAX "adapters" as XReferrableImpl, ProcessImpl, etc.
     * 
     * @param <T>
     * @param aModelInterfaceClass
     * @return
     */
    public abstract <T extends BioPAXElement> Class<T> getImplClass(Class<T> aModelInterfaceClass);
}
