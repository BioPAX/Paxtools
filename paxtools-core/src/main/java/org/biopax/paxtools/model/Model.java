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
     * This method add the given onject to this model. If the object
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
     * @param aClass a biopax model interface
     * @param id     of the new object
     * @return newly created object
     */
    <T extends BioPAXElement> T addNew(Class<T> aClass, String id);

    /**
     * This method returns true if the parameter is contained within
     * this model.
     *
     * @param aBioPAXElement to be checked
     * @return true if the parameter is in the object set
     */
    boolean contains(BioPAXElement aBioPAXElement);

    /**
     * This method returns the biopax element with the given id,
     * returns null if the object with the given id does not exist
     * in this model.
     * @param id of the object to be retrieved.
     * @return biopax element with the given id.
     */
    BioPAXElement getByID(String id);

    /**
     * This method checks for the biopax element with the given id,
     * returns true if the object with the given id exists.
     * in this model.
     * @param id of the object to be retrieved.
     * @return biopax element with the given id.
     */
    boolean containsID(String id);


    /**
     * This method returns a map of name space prefixes.
     * This map can be modified.
     * @return a map, mapping prefixes to full namespaces.
     */
    Map<String, String> getNameSpacePrefixMap();

// --------------------- ACCESORS and MUTATORS---------------------

    /**
     * This method returns a set of objects in the model.
     * Contents of this set can not be modified.
     * @return an unmodifiable set of objects.
     */
    Set<BioPAXElement> getObjects();

    /**
     * This method returns a set of objects in the model of the given class.
     * Contents of this set should not be modified.
     * @param filterBy class to be used as a filter.
     * @return an unmodifiable set of objects of the given class.
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
     * @param existing
     * @param replacement
     */
    void replace(BioPAXElement existing, BioPAXElement replacement);
    
    
    /**
     * Attempts to repair the model,
     * i.e., make it self-consistent, integral.
     * 
     */
    void repair();

}
