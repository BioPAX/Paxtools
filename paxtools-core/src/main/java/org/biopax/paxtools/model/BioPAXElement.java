package org.biopax.paxtools.model;

import java.io.Serializable;
import java.util.Map;

/**
 * This class represents a general BioPAXElement, regardless of Level.
 */
public interface BioPAXElement extends Serializable, Cloneable
{
// ------------------------------ FIELDS ------------------------------

    /**
     * Constant for representing unknown doubles. This is required
     * as by default java would assign 0.
     */
    public static final Double UNKNOWN_DOUBLE = Double.MIN_VALUE;

    /**
     * Constant for representing unknown floats. This is required
     * as by default java would assign 0.
     */
	public static final Float UNKNOWN_FLOAT = Float.MIN_VALUE;

    /**
     * Constant for representing unknown integers. This is required
     * as by default java would assign 0.
     */
	public static final Integer UNKNOWN_INT = Integer.MIN_VALUE;
	
	
    /**
     * This method returns the actual model interface that a class implements.
     * @return an interface from {@link org.biopax.paxtools.model} package
     * corresponding to a BioPAX class.
     */
    Class<? extends BioPAXElement> getModelInterface();

    /**
     * This method returns the RDF Id of the element. All data providers are
     * responsible for generating unique ids.
     * @return the unique rdf Id for this object.
     */
    String getRDFId();


    /**
     * This method compares the given element for equivalency. This is different
     * from equals(), as BioPAX elements resolve equality based on RDF ID.
     * Equivalent returns true if elements are equal or if
     *  <ul>
     *   <li> both elements implement the same model interface AND
     *   <li> both elements have equivalent <b>Key</b> properties
     *  </ul>
     *  These Key properties vary from class to class.
     *
     * @param element to be compared for equivalency
     * @return true if the element equals to this, or has equivalent critical
     * properties.
     */
    boolean isEquivalent(BioPAXElement element);

    /**
     * If two elements are equivalent, then their equivalence code should be the
     * same.
     * @return an integer that is same across all equivalent entities.
     */
     int equivalenceCode();

    /**
     * This annotation identifies properties that are
     */
    public @interface Key{}

    
    /**
     * A general-purpose map to optionallly 
     * store additional application-specific information 
     * about the BioPAX element, such as statistics,
     * inferred fields, etc.
     * 
     * @return
     */
    public Map<String, Object> getAnnotations();
	
}
