package org.biopax.paxtools.model.level3;

import java.util.Set;

/**
 * User: demir Date: Aug 15, 2007 Time: 8:08:52 PM
 */
public interface EntityReference extends UtilityClass, Named, Observable, XReferrable
{


	/**
	 * Variable features that are observed for the entities of this entityReference - such as known PTM
	 * or methylation sites and non-covalent bonds. Note that this is an aggregate list of all known
	 * features and it does not represent a state itself.
	 *
	 * @return a set of entityFeatures
	 */
	Set<EntityFeature> getEntityFeature();

	/**
	 * Variable features that are observed for the entities of this entityReference - such as known PTM
	 * or methylation sites and non-covalent bonds. Note that this is an aggregate list of all known
	 * features and it does not represent a state itself.
	 *
	 *  @param feature to be added.
	 */
	void addEntityFeature(EntityFeature feature);

	/**
	 * Variable features that are observed for the entities of this entityReference - such as known PTM
	 * or methylation sites and non-covalent bonds. Note that this is an aggregate list of all known
	 * features and it does not represent a state itself.
	 *
	 * @param feature to be removed.
	 */
	void removeEntityFeature(EntityFeature feature);

	/**
	 * Variable features that are observed for the entities of this entityReference - such as known PTM
	 * or methylation sites and non-covalent bonds. Note that this is an aggregate list of all known
	 * features and it does not represent a state itself.
	 *
	 * This method does not maintain bidirectional links and model consistency. Use add/remove
	 * feature instead.
	 *
	 * @param feature : A set of features to be set.
	 */
	void setEntityFeature(Set<EntityFeature> feature);

	/**
	 * Inverse of {@link SimplePhysicalEntity#getEntityReference()}
	 * @return a  set of {@link SimplePhysicalEntity} that has this EntityReference
	 */
	Set<SimplePhysicalEntity> getEntityReferenceOf();

	//property entityReferenceType

	Set<EntityReferenceTypeVocabulary> getEntityReferenceType();

	void addEntityReferenceType(EntityReferenceTypeVocabulary type);

	void removeEntityReferenceType(EntityReferenceTypeVocabulary type);

	void setEntityReferenceType(Set<EntityReferenceTypeVocabulary> type);


	Set<EntityReference> getMemberEntityReference();

	void addMemberEntityReference(EntityReference entity);

	void removeMemberEntityReference(EntityReference entity);

	void setMemberEntityReference(Set<EntityReference> entity);
}
