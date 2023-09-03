package org.biopax.paxtools.model;


import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * A model acts as a container for BioPAX elements.
 * Every object within a mode must have unique id.
 * A single object can be contained in multiple models.
 */
public interface Model extends Serializable
{
    /**
     * URI of this BioPAX Model.
     *
     * This can be used in many ways to refer to this model when:
     *  - logging;
     *  - merging several models;
     *  - converting to or from another data format, such as GMT (GSEA);
     *  - saving to or reading from RDF/XML file (e.g., save/read a special Provenance object)
     *
     * @return model's uri
     */
    String getUri();

    void setUri(String modelUri);

    /**
     * URI of this BioPAX Model.
     *
     * This can be used in many ways; see {@link #getUri()}.
     *
     * @return model's name
     */
    String getName();

    void setName(String modelName);

    /**
     * This method adds the given object to this model. If the object
     * points to other objects that are not in the model, it is user's
     * responsibility to add them into the model as well.
     * If an object with the same id already exists, it will throw
     * an {@link org.biopax.paxtools.util.IllegalBioPAXArgumentException}
     *
     * @param aBioPAXElement to be added
     */

    void add(BioPAXElement aBioPAXElement);


    /**
     * This method creates a new object using the model's factory, adds it
     * to the model and returns it.
     *
     * @param <T> a BioPAX type
     * @param aClass the BioPAX model interface class
     * @param id     of the new object
     * @return newly created object
     */
    <T extends BioPAXElement> T addNew(Class<T> aClass, String id);

    /**
     * This method returns true if the parameter is contained within
     * this model. 
     * 
     * Note: the result can be 'false' when 
     * {@link #containsID(String)} is 'true'
     * (using the URI of the parameter) if, e.g., 
     * model contains another object with the same URI.
     * 
     * @see #containsID(String)
     *
     * @param aBioPAXElement to be checked
     * @return true if the parameter is in the model
     */
    boolean contains(BioPAXElement aBioPAXElement);

    /**
     * This method returns the biopax element with the given URI,
     * returns null if the object with the given id does not exist
     * in this model.
     * @param id URI of the object to be retrieved.
     * @return biopax element with the given URI.
     */
    BioPAXElement getByID(String id);

    /**
     * This method checks for the biopax element with the given URI,
     * returns true if the object with the given id exists.
     * in this model.
     * @param id URI of the object to be retrieved.
     * @return biopax element with the given URI.
     */
    boolean containsID(String id);

    /**
     * This method returns a map of name space prefixes.
     * This map can be modified.
     * @return a map, mapping prefixes to full namespaces.
     */
    Map<String, String> getNameSpacePrefixMap();

// --------------------- ACCESSORS and MUTATORS---------------------

    /**
     * This method returns all the objects in the model.
     *
     * @return objects (unmodifiable set).
     */
    Set<BioPAXElement> getObjects();

    /**
     * This method returns the objects of the given class in the model.
     *
     * @param <T>      a BioPAX type
     * @param filterBy class to be used as a filter.
     * @return objects of the given class (unmodifiable set).
     */
    <T extends BioPAXElement> Set<T> getObjects(Class<T> filterBy);

    /**
     * This method removes the given BioPAX Element from the model.
     * Other objects in the model can still point to this object.
     * It is user's responsibility to properly excise these properties.
     * @param aBioPAXElement to be removed.
     */
    void remove(BioPAXElement aBioPAXElement);

    /**
     * This method sets the factory this model will use for creating
     * BioPAX objects. For example {@link #addNew(Class, String)} method
     * uses this factory.
     *
     * @param factory this model will use for creating
     * BioPAX objects.
     */
    void setFactory(BioPAXFactory factory);

   /**
     * This method returns the level of the objects that are
     * contained within this model.
     * @return level of the objects within this model.
     */
    BioPAXLevel getLevel();

    /**
     * When set to false, the model will not check for the dependent
     * objects of a newly added object. When true it will traverse and
     * add all dependent objects that are not already in the model.
     * This feature is currently experimental.
     *
     * @param value defining the dependency adding behaviour
     */
    void setAddDependencies(boolean value);

    /**
     * When addDependencies is false, the model will not check for the dependent
     * objects of a newly added object. When true it will traverse and
     * add all dependent objects that are not already in the model.
     * This feature is currently experimental.
     *
     * @return whether adding dependencies.
     */
    boolean isAddDependencies();

    
    /**
     * Merges the source model into this one.
     * 
     * @param source a model to merge
     */
    void merge(Model source);
    
    
    /**
     * Replaces existing BioPAX element with another one,
     * of the same or possibly equivalent type,
     * and updates all the affected references to it (object properties).
     * 
     * @param existing object to be replaced
     * @param replacement the replacement BioPAX object
     */
    void replace(BioPAXElement existing, BioPAXElement replacement);
    
    
    /**
     * Attempts to repair the model,
     * i.e., make it self-consistent, integral.
     */
    void repair();

    
    /**
     * Sets the xml:base to use when exporting a BioPAX model.
     * Usually, is is a data-provider's URI prefix, e.g.,
     * xml:base="http://www.pantherdb.org/pathways/biopax#" 
     * Setting this to null makes the exporter print using absolute
     * URIs (and rdf:about) instead of relative ones (and rdf:ID).
     * 
     * @param base a URI prefix or null.
     */
     void setXmlBase(String base);

     
     /**
      * Gets the model's xml:base (URI prefix/namespace), which
      * normally the majority of the BioPAX object's absolute URIs
      * in the model begin with.
      *
      * Note: it's not required that all the BioPAX objects
      * in the model have the same URI prefix/namespace;
      * e.g., there are can be (and perfectly legal) objects
      * that use other URI bases, such as identifiers.org/, bioregistry.io/,
      * http://purl.org/, etc. (- usually these are well-known
      * standard xml bases, or these result from merging several BioPAX
      * models of different data providers into one model.)
      *
      * @return xml:base value
      */
     String getXmlBase();

     int size();
}
