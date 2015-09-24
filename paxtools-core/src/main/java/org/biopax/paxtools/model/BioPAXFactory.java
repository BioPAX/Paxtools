package org.biopax.paxtools.model;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.impl.ModelImpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Abstract factory class for instantiating BioPAX classes. Different implementations of BioPAX model should also
 * implement their own factory
 */

public abstract class BioPAXFactory
{
    private static final Log log = LogFactory.getLog(BioPAXFactory.class);
	
    private final Method setUriMethod;
    
    /**
     * Protected Constructor without parameters.
     */
    protected BioPAXFactory() {
    	try {
			setUriMethod = BioPAXElementImpl.class.getDeclaredMethod("setUri", String.class);
			setUriMethod.setAccessible(true);
		} catch (Throwable e) {
			throw new RuntimeException("BioPAXFactory Constructor failed", e);
		} 
	}

    /**
     * Gets the level.
     * 
     * @return the biopax level
     */
    public abstract BioPAXLevel getLevel();

    
    public BioPAXElement create(String localName, String uri)
    {
        Class<? extends BioPAXElement> type = getLevel().getInterfaceForName(localName);
    	return create(type, uri);
    }

    
    /**
     * Universal method that creates a new BioPAX object.
     * (works with non-public, other package, implementations)
	 *
	 * @param <T> type
	 * @param aClass the class that corresponds to the BioPAX type
	 * @param uri absolute URI of the new BioPAX object
	 * @return new BioPAX object
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
				setUriMethod.invoke(bpe, uri);
			} else {
				log.error("Could not find a class implementing " + aClass);
				return null;
			}
		} catch (Exception e) {
			log.error("Could not instantiate BioPAX Type: " + aClass 
					+ "; URI: " + uri, e);
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
     * @param aClass BioPAX interface class
     * @return whether this factory can create an instance of the type
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


	/**
	 * Creates a new BioPAX model.
	 * @return BioPAX object model implementation
	 */
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
     * @param <T> BioPAX type/interface
     * @param aModelInterfaceClass interface class for the BioPAX type
     * @return concrete class that implements the BioPAX interface and can be created with this factory
     */
	public <T extends BioPAXElement> Class<T> getImplClass(
			Class<T> aModelInterfaceClass) 
	{
		Class<T> implClass = null;

		if (aModelInterfaceClass.isInterface()) {
			String name = mapClassName(aModelInterfaceClass);
			try {
				implClass = (Class<T>) Class.forName(name);
			} catch (ClassNotFoundException e) {
				log.error(String.format("getImplClass(%s), %s" , aModelInterfaceClass, e));
			}
		}

		return implClass;
	}

}
