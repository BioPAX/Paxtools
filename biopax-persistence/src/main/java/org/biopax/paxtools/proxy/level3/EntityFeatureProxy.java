/*
 * EntityFeatureProxy.java
 *
 * 2007.12.03 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;

import java.util.Set;

/**
 * Proxy for entityFeature
 */
@Entity(name="l3entityfeature")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class EntityFeatureProxy<T extends EntityFeature> extends Level3ElementProxy<T> implements EntityFeature {

// Observable

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = EvidenceProxy.class)
	@JoinTable(name="l3entityfeature_evidence")
	public Set<Evidence> getEvidence() {
		return object.getEvidence();
	}

	public void addEvidence(Evidence newEvidence) {
		object.addEvidence(newEvidence);
	}

	public void removeEvidence(Evidence oldEvidence) {
		object.removeEvidence(oldEvidence);
	}

	public void setEvidence(Set<Evidence> newEvidence) {
		object.setEvidence(newEvidence);
	}

	// Property FEATURE-LOCATION

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = SequenceLocationProxy.class)
	@JoinTable(name="l3entityfeature_feature_loc")
	public SequenceLocation getFeatureLocation() {
		return object.getFeatureLocation();
	}

	public void addFeatureLocation(SequenceLocation newFEATURE_LOCATION) {
		object.addFeatureLocation(newFEATURE_LOCATION);
	}

	public void removeFeatureLocation(SequenceLocation oldFEATURE_LOCATION) {
		object.removeFeatureLocation(oldFEATURE_LOCATION);
	}

	public void setFeatureLocation(Set<SequenceLocation> newFEATURE_LOCATION) {
		object.setFeatureLocation(newFEATURE_LOCATION);
	}

	// Property FEATURE-LOCATION-TYPE
	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = SequenceRegionVocabularyProxy.class)
	@JoinTable(name="l3entityfeature_feature_loc_type")
	public void setFeatureLocationType(SequenceRegionVocabulary regionVocabulary)
	{
		object.setFeatureLocationType(regionVocabulary);
	}

	public SequenceRegionVocabulary getFeatureLocationType()
	{
		return object.getFeatureLocationType();
	}
	
	// Inverse of Property ENTITY-FEATURE

	@Transient //todo map inverse dep..
	//@ManyToOne(targetEntity = EntityReferenceProxy.class)
	public EntityReference getEntityFeatureOf() {
		return object.getEntityFeatureOf();
	}

	public void setEntityFeatureOf(EntityReference newReferenceEntity) {
		object.setEntityFeatureOf(newReferenceEntity);
	}

	// Inverse of Property MODIFIED_AT

	@Transient
	public Set<PhysicalEntity> getFeatureOf() {
		return object.getFeatureOf();
	}

	// Inverse of Property NOT_MODIFIED_AT

	@Transient
	public Set<PhysicalEntity> getNoFeatureOf() {
		return object.getNoFeatureOf();
	}

	// MemberFeature

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = EntityFeatureProxy.class)
	@JoinTable(name="l3entityfeature_member_feature")
	public Set<EntityFeature> getMemberFeature() {
		return object.getMemberFeature();
	}

	public void addMemberFeature(EntityFeature entityFeature) {
		object.addMemberFeature(entityFeature);
	}

	public void removeMemberFeature(EntityFeature entityFeature) {
		object.removeMemberFeature(entityFeature);
	}

	public void setMemberFeature(Set<EntityFeature> entityFeature) {
		object.setMemberFeature(entityFeature);
	}

	public boolean atEquivalentLocation(EntityFeature that) {
		return object.atEquivalentLocation(that);
	}

	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return EntityFeature.class;
	}
}
