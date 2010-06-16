package org.biopax.paxtools.model.level3;

import java.util.Set;


public interface PhysicalEntity extends Entity, Controller
{


	// Inverse of COMPONENT

	Set<Complex> getComponentOf();


	/**
	 * A cellular location, e.g. 'cytoplasm'. This should reference a term in the <a
	 * href="http://www.obofoundry.org/cgi-bin/detail.cgi?id=cellular_component">Gene Ontology Cellular
	 * Component</a> ontology. The location referred to by this property should be as specific as is
	 * known. If an interaction is known to occur in multiple locations, separate interactions (and
	 * physicalEntities) must be created for each different location.  If the location of a participant
	 * in a complex is unspecified, it may be assumed to be the same location as that of the complex.
	 * <p/>
	 * A molecule in two different cellular locations are considered two different physical entities.
	 *
	 * @return cellular location of this physical entity
	 */
	CellularLocationVocabulary getCellularLocation();

	/**
	 * A cellular location, e.g. 'cytoplasm'. This should reference a term in the <a
	 * href="http://www.obofoundry.org/cgi-bin/detail.cgi?id=cellular_component">Gene Ontology Cellular
	 * Component</a> ontology. The location referred to by this property should be as specific as is
	 * known. If an interaction is known to occur in multiple locations, separate interactions (and
	 * physicalEntities) must be created for each different location.  If the location of a participant
	 * in a complex is unspecified, it may be assumed to be the same location as that of the complex.
	 * <p/>
	 * A molecule in two different cellular locations are considered two different physical entities.
	 *
	 * @param newCellularLocation for this physical entity
	 */
	void setCellularLocation(CellularLocationVocabulary newCellularLocation);

	boolean hasEquivalentFeatures(PhysicalEntity that);
	
	boolean hasEquivalentCellularLocation(PhysicalEntity that);
	
	// Property MODIFIED-AT

	Set<EntityFeature> getFeature();

	void addFeature(EntityFeature feature);

	void removeFeature(EntityFeature feature);



	// Property NOT-MODIFIED-AT

	Set<EntityFeature> getNotFeature();

	void addNotFeature(EntityFeature feature);

	void removeNotFeature(EntityFeature feature);



	//Property memberPhysicalEntity

	Set<PhysicalEntity> getMemberPhysicalEntity();

	void addMemberPhysicalEntity(PhysicalEntity memberPhysicalEntity);

	void removeMemberPhysicalEntity(PhysicalEntity memberPhysicalEntity);



	//Inverse of property memberPhysicalEntity
	Set<PhysicalEntity> getMemberPhysicalEntityOf();



	/**
	 * Overridden to provide better downcasting
	*/
	Class<? extends PhysicalEntity> getModelInterface();
}
	
