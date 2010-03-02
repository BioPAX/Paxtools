/*
 * EntityFeatureProxy.java
 *
 * 2007.12.03 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;

import java.util.Set;

/**
 * Proxy for entityFeature
 */
@Entity(name="l3entityfeature")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class EntityFeatureProxy extends Level3ElementProxy implements EntityFeature {
	public EntityFeatureProxy() {
	}

// Observable

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = EvidenceProxy.class)
	@JoinTable(name="l3entityfeature_evidence")
	public Set<Evidence> getEvidence() {
		return ((EntityFeature)object).getEvidence();
	}

	public void addEvidence(Evidence newEvidence) {
		((EntityFeature)object).addEvidence(newEvidence);
	}

	public void removeEvidence(Evidence oldEvidence) {
		((EntityFeature)object).removeEvidence(oldEvidence);
	}

	public void setEvidence(Set<Evidence> newEvidence) {
		((EntityFeature)object).setEvidence(newEvidence);
	}

	// Property FEATURE-LOCATION

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = SequenceLocationProxy.class)
	@JoinTable(name="l3entityfeature_feature_loc")
	public Set<SequenceLocation> getFeatureLocation() {
		return ((EntityFeature)object).getFeatureLocation();
	}

	public void addFeatureLocation(SequenceLocation newFEATURE_LOCATION) {
		((EntityFeature)object).addFeatureLocation(newFEATURE_LOCATION);
	}

	public void removeFeatureLocation(SequenceLocation oldFEATURE_LOCATION) {
		((EntityFeature)object).removeFeatureLocation(oldFEATURE_LOCATION);
	}

	public void setFeatureLocation(Set<SequenceLocation> newFEATURE_LOCATION) {
		((EntityFeature)object).setFeatureLocation(newFEATURE_LOCATION);
	}

	// Property FEATURE-LOCATION-TYPE
	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = SequenceRegionVocabularyProxy.class)
	@JoinTable(name="l3entityfeature_feature_loc_type")
	public void setFeatureLocationType(SequenceRegionVocabulary regionVocabulary)
	{
		((EntityFeature)object).setFeatureLocationType(regionVocabulary);
	}

	public SequenceRegionVocabulary getFeatureLocationType()
	{
		return ((EntityFeature)object).getFeatureLocationType();
	}
	
	// Inverse of Property ENTITY-FEATURE

	@Transient
	public EntityReference getEntityFeatureOf() {
		return ((EntityFeature)object).getEntityFeatureOf();
	}

	public void setEntityFeatureOf(EntityReference newReferenceEntity) {
		((EntityFeature)object).setEntityFeatureOf(newReferenceEntity);
	}

	// Inverse of Property MODIFIED_AT

	@Transient
	public Set<PhysicalEntity> getFeatureOf() {
		return ((EntityFeature)object).getFeatureOf();
	}

	// Inverse of Property NOT_MODIFIED_AT

	@Transient
	public Set<PhysicalEntity> getNoFeatureOf() {
		return ((EntityFeature)object).getNoFeatureOf();
	}

	// MemberFeature

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = EntityFeatureProxy.class)
	@JoinTable(name="l3entityfeature_member_feature")
	public Set<EntityFeature> getMemberFeature() {
		return ((EntityFeature)object).getMemberFeature();
	}

	public void addMemberFeature(EntityFeature entityFeature) {
		((EntityFeature)object).addMemberFeature(entityFeature);
	}

	public void removeMemberFeature(EntityFeature entityFeature) {
		((EntityFeature)object).removeMemberFeature(entityFeature);
	}

	public void setMemberFeature(Set<EntityFeature> entityFeature) {
		((EntityFeature)object).setMemberFeature(entityFeature);
	}

	public boolean atEquivalentLocation(EntityFeature that) {
		return ((EntityFeature)object).atEquivalentLocation(that);
	}

	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return EntityFeature.class;
	}
}
