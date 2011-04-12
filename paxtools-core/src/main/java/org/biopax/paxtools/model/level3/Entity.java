package org.biopax.paxtools.model.level3;

import java.util.Set;

/**
 * This class represents a discrete biological unit used when describing
 * pathways. This is the root class for all biological concepts in the ontology,
 * which include pathways, interactions and physical entities. As the most
 * abstract class in the ontology, instances of the entity class should never be
 * created. Instead, more specific classes should be used. Synonyms: thing,
 * object, bioentity.
 */
public interface Entity extends Level3Element, Observable, Named
{
	// --------------------- ACCESORS and MUTATORS---------------------

	// Property AVAILABILITY

	/**
	 * The contents of this set can be modified but semantic consistency is not
	 * guaranteed. Using {@link #addAvailability(String)} and {@link
	 * #removeAvailability(String)} is recommended.
	 * @return a set of  strings describing the availability of this data (e.g. a
	 *         copyright statement).
	 */
	public Set<String> getAvailability();


	/**
	 * This method adds the given text to the avaialability set.
	 * @param availability a string describing the availability of this data
	 * (e.g. a copyright statement).
	 */
	public void addAvailability(String availability);

	/**
	 * This method removes the given text from the avaialability set.
	 * @param availability a string describing the availability of this data
	 * (e.g. a copyright statement).
	 */
	public void removeAvailability(String availability);


	// Property DATA-SOURCE

	/**
	 * This method returns a set of free text descriptions of the source of this
	 * data, e.g. a database or person name. This property should be used to
	 * describe the source of the data. This is meant to be used by databases that
	 * export their data to the BioPAX format or by systems that are integrating
	 * data from multiple sources. The granularity of use (specifying the data
	 * source in many or few instances) is up to the user. It is intended that
	 * this property report the last data source, not all data sources that the
	 * data has passed through from creation.
	 * <p/>
	 * The contents of this set can be modified but semantic consistency is not
	 * guaranteed. Using {@link #addDataSource(Provenance)} and {@link #removeDataSource(Provenance)} is
	 * recommended.
	 * @return a set of free text descriptions of the source of this data, e.g. a
	 *         database or person name.
	 */
	public Set<Provenance> getDataSource();


	/**
	 * This method adds the given value to the DATA_SOURCE set.
	 * @param dataSource a free text description of the source of this data,
	 * e.g. a database or person name.
	 */
	public void addDataSource(Provenance dataSource);

	/**
	 * This method removes the given value from the DATA_SOURCE set.
	 * @param dataSource a free text description of the source of this data,
	 * e.g. a database or person name.
	 */
	public void removeDataSource(Provenance dataSource);




	/**
	 * This method  returns the interaction that this entity/pep takes part in.
	 * Contents of this set should not be modified.
	 * Reverse of {@link org.biopax.paxtools.model.level3.Interaction#getParticipant()}
	 * @return a set of interactions that
	 */
	public Set<Interaction> getParticipantOf();

}
