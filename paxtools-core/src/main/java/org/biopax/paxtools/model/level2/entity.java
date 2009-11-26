package org.biopax.paxtools.model.level2;

import java.util.Set;

/**
 * This class represents a discrete biological unit used when describing
 * pathways. This is the root class for all biological concepts in the ontology,
 * which include pathways, interactions and physical entities. As the most
 * abstract class in the ontology, instances of the entity class should never be
 * created. Instead, more specific classes should be used. Synonyms: thing,
 * object, bioentity.
 */
public interface entity
	extends Level2Element, XReferrable, InteractionParticipant
{
// -------------------------- OTHER METHODS --------------------------

	/**
	 * This method adds the given text to the avaialability set.
	 *
	 * @param AVAILABILITY_TEXT a string describing the availability of this data
	 *                          (e.g. a copyright statement).
	 */
	public void addAVAILABILITY(String AVAILABILITY_TEXT);

	/**
	 * This method adds the given value to the DATA_SOURCE set.
	 *
	 * @param DATA_SOURCE_INST a free text description of the source of this data,
	 *                         e.g. a database or person name.
	 */
	public void addDATA_SOURCE(dataSource DATA_SOURCE_INST);

	/**
	 * This method adds the given value to the SYNONYMS set.
	 *
	 * @param SYNONYMS_TEXT a new name to be added
	 */
	public void addSYNONYMS(String SYNONYMS_TEXT);
// --------------------- ACCESORS and MUTATORS---------------------

	/**
	 * The contents of this set can be modified but semantic consistency is not
	 * guaranteed. Using {@link #addAVAILABILITY(String)} and {@link
	 * #removeAVAILABILITY(String)} is recommended.
	 *
	 * @return a set of  strings describing the availability of this data (e.g. a
	 *         copyright statement).
	 */

	public Set<String> getAVAILABILITY();


	/**
	 * This method returns a set of free text descriptions of the source of this
	 * data, e.g. a database or person name. This property should be used to
	 * describe the source of the data. This is meant to be used by databases that
	 * export their data to the BioPAX format or by systems that are integrating
	 * data from multiple sources. The granularity of use (specifying the data
	 * source in many or few instances) is up to the user. It is intended that this
	 * property report the last data source, not all data sources that the data has
	 * passed through from creation.
	 * <p/>
	 * The contents of this set can be modified but semantic consistency is not
	 * guaranteed. Using {@link #addDATA_SOURCE} and {@link #removeDATA_SOURCE} is
	 * recommended.
	 *
	 * @return a set of free text descriptions of the source of this data, e.g. a
	 *         database or person name.
	 */
	public Set<dataSource> getDATA_SOURCE();


	/**
	 * This method returns the preferred full name for this entity.
	 *
	 * @return preferred full name for this entity
	 */
	public String getNAME();

	/**
	 * This method sets an abbreviated name for this entity, preferably a name that
	 * is short enough to be used in a visualization application to label a
	 * graphical element that represents this entity. If no short name is
	 * available, an xref may be used for this purpose by the visualization
	 * application.
	 *
	 * @return an abbreviated name suitable for display.
	 */
	public String getSHORT_NAME();

	/**
	 * The contents of this set can be modified but semantic consistency is not
	 * guaranteed. Using {@link #addSYNONYMS} and {@link #removeSYNONYMS} is
	 * recommended.
	 *
	 * @return a set of synonyms for the name of this entity. This should include
	 *         the values of the NAME and SHORT-NAME property so that it is easy to
	 *         find all known names in one place.
	 */
	public Set<String> getSYNONYMS();

	/**
	 * This method removes the given text from the avaialability set.
	 *
	 * @param AVAILABILITY_TEXT a string describing the availability of this data
	 *                          (e.g. a copyright statement).
	 */
	public void removeAVAILABILITY(String AVAILABILITY_TEXT);

	/**
	 * This method removes the given value from the DATA_SOURCE set.
	 *
	 * @param DATA_SOURCE_INST a free text description of the source of this data,
	 *                         e.g. a database or person name.
	 */
	public void removeDATA_SOURCE(dataSource DATA_SOURCE_INST);

	/**
	 * This method removes the given value from the SYNONYMS set.
	 *
	 * @param SYNONYMS_TEXT a new name to be added
	 */
	public void removeSYNONYMS(String SYNONYMS_TEXT);

	/**
	 * This method overrides existing set with the new set. If you want to append
	 * to the existing set, use {@link #addAVAILABILITY} instead.
	 *
	 * @param AVAILABILITY_TEXT a set of strings describing the availability of
	 *                          this data (e.g. a copyright statement).
	 */
	public void setAVAILABILITY(Set<String> AVAILABILITY_TEXT);

	/**
	 * This method overrides existing set with the new set. If you want to append
	 * to the existing set, use {@link #addDATA_SOURCE} instead.
	 *
	 * @param DATA_SOURCE a set of free text descriptions of the source of this
	 *                    data, e.g. a database or person name.
	 */
	public void setDATA_SOURCE(Set<dataSource> DATA_SOURCE);

	/**
	 * This method sets the preferred full name for this entity to the given
	 * value.
	 *
	 * @param NAME The preferred full name for this entity.
	 */
	public void setNAME(String NAME);

	/**
	 * An abbreviated name for this entity, preferably a name that is short enough
	 * to be used in a visualization application to label a graphical element that
	 * represents this entity. If no short name is available, an xref may be used
	 * for this purpose by the visualization application.
	 *
	 * @param SHORT_NAME new short name
	 */
	public void setSHORT_NAME(String SHORT_NAME);

	/**
	 * This method overrides existing set with the new set. If you want to append
	 * to the existing set, use {@link #addSYNONYMS} instead.
	 *
	 * @param SYNONYMS a set of names for this entity.
	 */
	public void setSYNONYMS(Set<String> SYNONYMS);
}