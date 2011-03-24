package org.biopax.paxtools.model;

/**
 * A BioPAXFactory is the preferred method of creating BioPAX objects.
 * This interface allows reflective creation of the objects.
 * Strongly typed creation methods are implemented in level specific factories.
 * @see org.biopax.paxtools.model.level2.Level2Factory
 * @see org.biopax.paxtools.model.level3.Level3Factory
 *
 */
public interface BioPAXFactory
{


    /**
     * @return the BioPAX level this factory was written for.
     */
    BioPAXLevel getLevel();

    /**
     * @return a new BioPAX model
     */
    Model createModel();


    /**
     * This method will create and return a new instance of the class of the
     * given name. If the name is not defined in the BioPAX ontology, it will
     * throw an exception.
     * @param name of the class. Case is important.
     * @param uri elements id (rdfID which is URI)
     * @return a new instance of the class of the given name as defined by
     * BioPAX ontology
     */
    BioPAXElement reflectivelyCreate(String name, String uri);

    /**
     * This method will create and return a new instance of the given class.
     * If the class is not defined in the API, it will throw an
     * exception.
     * @param aClass a BioPAX model interface
     * @param uri rdfid
     * @return a new instance of the class as defined by BioPAX ontology
     */
    <T extends BioPAXElement> T  reflectivelyCreate(Class<T> aClass, String uri);
	
    
    /**
     * This method will return true, if this factory can create a new instance
     * of the class defined by the name.
     * @param name of the BioPAX class
     * @return true if this factory can instatiate an instance of this class.
     */
    boolean canInstantiate(String name);
    
}
