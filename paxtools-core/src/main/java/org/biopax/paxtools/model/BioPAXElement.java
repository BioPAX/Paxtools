package org.biopax.paxtools.model;

import java.io.Serializable;

/**
 * This class represents a general BioPAXElement, regardless of Level.
 */
public interface BioPAXElement extends Serializable
{
// ------------------------------ FIELDS ------------------------------

    /**
     * Constant for representing unknown doubles. This is required
     * as by default java would assign 0.
     */
    public static final double UNKNOWN_DOUBLE = Double.NaN;

    /**
     * Constant for representing unknown floats. This is required
     * as by default java would assign 0.
     */
	public static final float UNKNOWN_FLOAT = Float.NaN;

    /**
     * Constant for representing unknown integers. This is required
     * as by default java would assign 0.
     */
	public static final int UNKNOWN_INT = Integer.MIN_VALUE;
	
	
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
     * This method sets the RDF id of the element.  All data providers are
     * responsible for generating unique ids.
     * @param id  the unique rdf Id for this object.
     */
    void setRDFId(String id);


    /**
     * This method compare the given element for equivalency. This is different
     * from equals(), as BioPAX elements resolve equality based on RDF ID.
     * Equivalent returns true if elements are equal or if
     *  <ul>
     *   <li> both elements implement the same model interface AND
     *   <li> both elements have equivalent <b>critical</b> properties
     *  </ul>
     *  What these critical properties are varies from class to class.
     *
     * @param element to be compared for equivalancy
     * @return true if the element equals to this, or has equivalant critical
     * properties.
     */
    boolean isEquivalent(BioPAXElement element);

    /**
     * If two elements are equivalent, then their equivalance code should be the
     * same.
     * @return an integer that is same across all equivalent entities.
     */
	int equivalenceCode();
	
}
