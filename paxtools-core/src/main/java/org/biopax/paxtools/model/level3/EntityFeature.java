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


	// Inverse of Property MODIFIED_AT

	Set<PhysicalEntity> getFeatureOf();

	// Inverse of Property NOT_MODIFIED_AT

	Set<PhysicalEntity> getNotFeatureOf();

	// Property featureLocation

	SequenceLocation getFeatureLocation();

	void setFeatureLocation(SequenceLocation sequenceLocation);

	SequenceRegionVocabulary getFeatureLocationType();

	void setFeatureLocationType(SequenceRegionVocabulary regionVocabulary);


	Set<EntityFeature> getMemberFeature();   //todo generify?

	void addMemberFeature(EntityFeature entityFeature);

	void removeMemberFeature(EntityFeature entityFeature);


	boolean atEquivalentLocation(EntityFeature that);
}
