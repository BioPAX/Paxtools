package org.biopax.paxtools.model.level3;


import java.util.Set;

/**
 * A feature or aspect of a physical entity that can be changed while the entity still retains its
 * biological identity.
 */
public interface EntityFeature extends UtilityClass, Observable
{


	// Inverse of Property ENTITY-FEATURE

	EntityReference getEntityFeatureOf();

	void setEntityFeatureOf(EntityReference newReferenceEntity);

	// Inverse of Property MODIFIED_AT

	Set<PhysicalEntity> getFeatureOf();

	// Inverse of Property NOT_MODIFIED_AT

	Set<PhysicalEntity> getNoFeatureOf();

	// Property featureLocation

	Set<SequenceLocation> getFeatureLocation();

	void addFeatureLocation(SequenceLocation featureLocation);

	void removeFeatureLocation(SequenceLocation featureLocation);

	void setFeatureLocation(Set<SequenceLocation> featureLocation);


	SequenceRegionVocabulary getFeatureLocationType();

	void setFeatureLocationType(SequenceRegionVocabulary regionVocabulary);


	Set<EntityFeature> getMemberFeature();

	void addMemberFeature(EntityFeature entityFeature);

	void removeMemberFeature(EntityFeature entityFeature);

	void setMemberFeature(Set<EntityFeature> entityFeature);

	boolean atEquivalentLocation(EntityFeature that);
}
