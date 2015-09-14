package org.biopax.paxtools.controller;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.BioPAXLevel;

import java.util.Iterator;
import java.util.Set;

/**
 * This class contains methods that eases to use editors for
 * specific or a set of property. Using the methods of this class
 * editors of a class, editors of a property, and editors of a property
 * with the given domain can be obtained; and these editors/this editor
 * can be used to modify object's properties.
 *
 * The functionallity of this class plays key roles on several other classes'
 * functionallity; e.g. {@link org.biopax.paxtools.io}, {@link org.biopax.paxtools.model},

 */
public interface EditorMap
{

// -------------------------- OTHER METHODS --------------------------

    /**
     * This method returns the <em>editor</em> intended to handle
     * property named <em>property</em> of a class (<em>javaClass</em>).
     * This editor can then be used to modify the property of
     * an element of class <em>javaClass</em>.
     *
     * To put in other words, this methods returns the editor of which
     * domain includes <em>javaClass</em>, and the editor that can handle
     * the <em>property</em>.
     *
     * @param property name of the property for which editor will be called
     * @param javaClass class of the element
     * @param <D> domain
     * @return null if there is no such editor
     */
	<D extends BioPAXElement> PropertyEditor<? super D,?> getEditorForProperty(String property, Class<D> javaClass);

    /**
     * This method returns the set of <em>editor</em>s intended to handle
     * property named <em>property</em>. This editor can then be used
     * to modify the property of an element which is in editor's domain list.
     *
     * In other words, this methods returns the set of the editors
     * that can handle the <em>property</em>. Editors are not filtered for
     * a specific domain class.
     *
     * @param property name of the property for which editor will be called
     * @return empty set if there are no such editors
     */
    Set<PropertyEditor> getEditorsForProperty(String property);

	/**
	  * This method returns the set of <em>editor</em>s intended to handle
	  * property named <em>property</em>. This editor can then be used
	  * to modify the property of an element which is in editor's domain list.
	  *
	  * In other words, this methods returns the set of the editors
	  * that can handle the <em>property</em>. Editors are not filtered for
	  * a specific domain class.
	  *
	  * @param property name of the property for which editor will be called
	  * @param domain biopax type/class the property belongs to
	  * @param <D> domain biopax type
	  * @return empty set if there are no such editors
	  */

	public <D extends BioPAXElement> Set<PropertyEditor<? extends D, ?>> getSubclassEditorsForProperty(
			String property, Class<D> domain);

	/**
     * This method returns the set of <em>editor</em>s whose domain
     * subsumes the class of given BioPAX element.
     *
	 * @param bpe BioPAX element for which the available editors will be returned
	 * @return empty set if there are no such editors
     */
    Set<PropertyEditor> getEditorsOf(BioPAXElement bpe);

	/**
	 * Properties in BioPAX specification is unidirectional. e.g. entityReference property that links a
	 * PhysicalEntity to EntityReference has no defined corresponding inverse property that links
	 * EntityReferences to their corresponding entities.
	 *
	 * Most OWL reasoners can query the inverse of a property at no additional cost, but for most OO implementations
	 * this would require an expensive O(n) lookup.
	 * An OO implementation requires keeping additional properties for efficiency purposes.
	 * Inverse editors are read-only editors that captures these "inverse" part of the bidirectional properties
	 * specifically implemented in Paxtools. They have the pattern <em>PropertyName</em>Of  e.g. entityReferenceOf.
	 * @param bpe  BioPAX element for which the available inverse editors will be returned.
	 * @return all inverse editors  for this entity's class type.
	 */
	Set<ObjectPropertyEditor> getInverseEditorsOf(BioPAXElement bpe);


    /**
     * Returns a set of sub classes of a given class. This method
     * can be used for class filtering methods.
     *
     * @param javaClass the class whose subclasses will be returned
     * @param <E> biopax type (biopax object model interface)
     * @return an empty set if there are no such editors
     */
    <E extends BioPAXElement> Set<? extends Class<E>> getKnownSubClassesOf(Class<E> javaClass);


    /**
     * Returns the BioPAX level for which editor map is created. Different
     * BioPAX levels have different editor maps.
     *
     * @return BioPAX Level
     */
    BioPAXLevel getLevel();

	/**
	 * This method returns the set of <em>editor</em>s whose domain
	 * subsumes the given class
	 *
	 * @param domain BioPAX model interface for which the available editors will be returned
	 * @return empty set if there are no such editors
	 */
	Set<PropertyEditor> getEditorsOf(Class<? extends BioPAXElement> domain);

	/**
	 * Properties in BioPAX specification is unidirectional. e.g. entityReference property that links a
	 * PhysicalEntity to EntityReference has no defined corresponding inverse property that links
	 * EntityReferences to their corresponding entities.
	 *
	 * Most OWL reasoners can query the inverse of a property at no additional cost, but for most OO implementations
	 * this would require an expensive O(n) lookup.
	 * An OO implementation requires keeping additional properties for efficiency purposes.
	 * Inverse editors are read-only editors that captures these "inverse" part of the bidirectional properties
	 * specifically implemented in Paxtools. They have the pattern <em>PropertyName</em>Of  e.g. entityReferenceOf.
	 * @param domain of the inverse property
	 * @return all inverse editors  for this class type.
	 */
	Set<ObjectPropertyEditor> getInverseEditorsOf(Class<? extends BioPAXElement> domain);

	/**
	 * @return An iterator over all the properties in this EditorMap
	 */
	public Iterator<PropertyEditor> iterator();

}
