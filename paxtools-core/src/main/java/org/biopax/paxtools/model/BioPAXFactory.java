package org.biopax.paxtools.model;


import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.impl.ModelImpl;

public abstract class BioPAXFactory
{
    private static Log log = LogFactory.getLog(BioPAXFactory.class);

    /**
     * Gets the level.
     * 
     * @return
     */
    public abstract BioPAXLevel getLevel();

    
    public BioPAXElement create(String localName, String uri)
    {
        return this.create((this.getLevel().getInterfaceForName(localName)), uri);
    }

    
    /*
     * Universal method that creates a new BioPAX object.
     * (works with non-public, other package, implementations;
     * so it's important to keep this method private)
     */
	public <T extends BioPAXElement> T create(Class<T> aClass, String uri) {
		T bpe = null;

		// create a new instance of the BioPAX type
		try {
			Class<T> t = getImplClass(aClass);
			if(t != null) {
				Constructor<T> c = t.getDeclaredConstructor();
				c.setAccessible(true);
				bpe = (T) c.newInstance();
			} else {
				log.error("Could not create a class " + aClass);
			}
		} catch (Exception e) {
			log.error("Could not instantiate " + aClass);
			log.error(e.getStackTrace());
		} 

		// set URI
		try {
			Method m = BioPAXElementImpl.class.getDeclaredMethod("setRDFId",
					String.class);
			m.setAccessible(true);
			m.invoke(bpe, uri);
		} catch (Exception e) {
			log.error("Could not set URI for " + bpe.getClass());
			log.error(e.getStackTrace());
			return null;
		}

		return bpe;
	}

    
	/**
	 * Maps a BioPAX type (model interface) to the
	 * full-qualified class name of an implementing class.
	 * BioPAX factories have to implement this method.
	 * 
	 * @param aClass BioPAX type (model interface)
	 * @return full class name
	 */
    public abstract String mapClassName(Class<? extends BioPAXElement> aClass);

    
    /**
     * Checks whether objects of a BioPAX model interface
     * can be created (some types are not official BioPAX 
     * types, abstract classes).
     * 
     * @param aClass
     * @return
     */
    public boolean canInstantiate(Class<? extends BioPAXElement> aClass) 
    {
        try {
            String cname = mapClassName(aClass);
        	return !Modifier.isAbstract(Class.forName(cname).getModifiers());
        } catch (ClassNotFoundException e)
        {
            return false;
        } catch (Exception ex) {
        	log.error("Error in canInstantiate(" + aClass + ")", ex);
			return false;
		}
    }


    public Model createModel() {
        return new ModelImpl(this);
    }

    
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
