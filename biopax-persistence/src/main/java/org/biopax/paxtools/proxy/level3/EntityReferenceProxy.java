/*
 * EntityReferenceProxy.java
 *
 * 2008.02.26 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.util.Set;
import org.biopax.paxtools.proxy.StringSetBridge;

/**
 * Proxy for EntityReference
 */
@javax.persistence.Entity(name="l3entityreference")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class EntityReferenceProxy extends Level3ElementProxy implements EntityReference {
	protected EntityReferenceProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return EntityReference.class;
	}

// Observable

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = EvidenceProxy.class)
	@JoinTable(name="l3entityref_evidence")
	public Set<Evidence> getEvidence() {
		return ((EntityReference)object).getEvidence();
	}

	public void addEvidence(Evidence newEvidence) {
		((EntityReference)object).addEvidence(newEvidence);
	}

	public void removeEvidence(Evidence oldEvidence) {
		((EntityReference)object).removeEvidence(oldEvidence);
	}

	public void setEvidence(Set<Evidence> newEvidence) {
		((EntityReference)object).setEvidence(newEvidence);
	}

// Named
	
	@CollectionOfElements @Column(name="name_x", columnDefinition="text")
	@FieldBridge(impl=StringSetBridge.class)
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_AVAILABILITY, index=Index.TOKENIZED)
	public Set<String> getName() {
		return ((EntityReference)object).getName();
	}
	
	public void addName(String NAME_TEXT) {
		((EntityReference)object).addName(NAME_TEXT);
	}
	
	public void removeName(String NAME_TEXT) {
		((EntityReference)object).removeName(NAME_TEXT);
	}
	
	public void setName(Set<String> newNAME) {
		((EntityReference)object).setName(newNAME);
	}
	
	@Basic @Column(name="display_name_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_NAME, index=Index.TOKENIZED)
	public String getDisplayName() {
		return ((EntityReference)object).getDisplayName();
	}
	
	public void setDisplayName(String newDISPLAY_NAME) {
		((EntityReference)object).setDisplayName(newDISPLAY_NAME);
	}
	
	@Basic @Column(name="standard_name_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_NAME, index=Index.TOKENIZED)
	public String getStandardName() {
		return ((EntityReference)object).getStandardName();
	}
	
	public void setStandardName(String newSTANDARD_NAME) {
		((EntityReference)object).setStandardName(newSTANDARD_NAME);
	}

// XReferrable

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = XrefProxy.class)
	@JoinTable(name="l3entityref_xref")
	public Set<Xref> getXref() {
		return ((EntityReference)object).getXref();
	}

	public void addXref(Xref XREF) {
		((EntityReference)object).addXref(XREF);
	}

	public void removeXref(Xref XREF) {
		((EntityReference)object).removeXref(XREF);
	}

	public void setXref(Set<Xref> XREF) {
		((EntityReference)object).setXref(XREF);
	}

// EntityReference

 	// Property EntityFeature

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = EntityFeatureProxy.class)
	@JoinTable(name="l3entityref_entity_feature")
	public Set<EntityFeature> getEntityFeature() {
		return ((EntityReference)object).getEntityFeature();
	}

	public void addEntityFeature(EntityFeature feature) {
		((EntityReference)object).addEntityFeature(feature);
	}

	public void removeEntityFeature(EntityFeature feature) {
		((EntityReference)object).removeEntityFeature(feature);
	}

	public void setEntityFeature(Set<EntityFeature> feature) {
		((EntityReference)object).setEntityFeature(feature);
	}

	//

	@Transient
	public Set<SimplePhysicalEntity> getEntityReferenceOf() {
		return ((EntityReference)object).getEntityReferenceOf();
	}

	//property entityReferenceType
	// Property GroupType

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = EntityReferenceTypeVocabularyProxy.class)
	@JoinTable(name="l3entityref_entity_ref_type")
    public Set<EntityReferenceTypeVocabulary> getEntityReferenceType() {
		return ((EntityReference)object).getEntityReferenceType();
    }

    public void addEntityReferenceType(EntityReferenceTypeVocabulary type) {
		((EntityReference)object).addEntityReferenceType(type);
    }

    public void removeEntityReferenceType(EntityReferenceTypeVocabulary type) {
		((EntityReference)object).removeEntityReferenceType(type);
    }

    public void setEntityReferenceType(Set<EntityReferenceTypeVocabulary> type) {
		((EntityReference)object).setEntityReferenceType(type);
    }

 	// Property MemberEntityReference

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = EntityReferenceProxy.class)
	@JoinTable(name="l3entityref_member_entity_ref")
    public Set<EntityReference> getMemberEntityReference() {
		return ((EntityReference)object).getMemberEntityReference();
    }

    public void addMemberEntityReference(EntityReference entity) {
		((EntityReference)object).addMemberEntityReference(entity);
    }

    public void removeMemberEntityReference(EntityReference entity) {
		((EntityReference)object).removeMemberEntityReference(entity);
    }

	public void setMemberEntityReference(Set<EntityReference> entity) {
		((EntityReference)object).setMemberEntityReference(entity);
    }
}

