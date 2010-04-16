/*
 * EntityReferenceProxy.java
 *
 * 2008.02.26 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.search.annotations.*;

import javax.persistence.*;

import java.util.Set;

import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.biopax.paxtools.proxy.StringSetBridge;

/**
 * Proxy for EntityReference
 */
@javax.persistence.Entity(name="l3entityreference")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class EntityReferenceProxy<T extends EntityReference>
        extends XReferrableProxy<T> implements EntityReference {

// Observable

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = EvidenceProxy.class)
	@JoinTable(name="l3entityref_evidence")
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

	@CollectionOfElements @Column(name="name_x", columnDefinition="text")
	@FieldBridge(impl=StringSetBridge.class)
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_NAME, index=Index.TOKENIZED)
	public Set<String> getName() {
		return object.getName();
	}
	
	public void addName(String NAME_TEXT) {
		object.addName(NAME_TEXT);
	}
	
	public void removeName(String NAME_TEXT) {
		object.removeName(NAME_TEXT);
	}
	
	public void setName(Set<String> newNAME) {
		object.setName(newNAME);
	}
	
	@Basic @Column(name="display_name_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_NAME, index=Index.TOKENIZED)
	public String getDisplayName() {
		return object.getDisplayName();
	}
	
	public void setDisplayName(String newDISPLAY_NAME) {
		object.setDisplayName(newDISPLAY_NAME);
	}
	
	@Basic @Column(name="standard_name_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_NAME, index=Index.TOKENIZED)
	public String getStandardName() {
		return object.getStandardName();
	}
	
	public void setStandardName(String newSTANDARD_NAME) {
		object.setStandardName(newSTANDARD_NAME);
	}


// EntityReference

 	// Property EntityFeature

	@OneToMany(cascade = {CascadeType.ALL}, targetEntity = EntityFeatureProxy.class)
	public Set<EntityFeature> getEntityFeature() {
		return object.getEntityFeature();
	}

	public void addEntityFeature(EntityFeature feature) {
		object.addEntityFeature(feature);
	}

	public void removeEntityFeature(EntityFeature feature) {
		object.removeEntityFeature(feature);
	}

	public void setEntityFeature(Set<EntityFeature> feature) {
		object.setEntityFeature(feature);
	}

	//

	@Transient
	public Set<SimplePhysicalEntity> getEntityReferenceOf() {
		return object.getEntityReferenceOf();
	}

	//property entityReferenceType
	// Property GroupType

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = EntityReferenceTypeVocabularyProxy.class)
	@JoinTable(name="l3entityref_entity_ref_type")
    public Set<EntityReferenceTypeVocabulary> getEntityReferenceType() {
		return object.getEntityReferenceType();
    }

    public void addEntityReferenceType(EntityReferenceTypeVocabulary type) {
		object.addEntityReferenceType(type);
    }

    public void removeEntityReferenceType(EntityReferenceTypeVocabulary type) {
		object.removeEntityReferenceType(type);
    }

    public void setEntityReferenceType(Set<EntityReferenceTypeVocabulary> type) {
		object.setEntityReferenceType(type);
    }

 	// Property MemberEntityReference

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = EntityReferenceProxy.class)
	@JoinTable(name="l3entityref_member_entity_ref")
    public Set<EntityReference> getMemberEntityReference() {
		return object.getMemberEntityReference();
    }

    public void addMemberEntityReference(EntityReference entity) {
		object.addMemberEntityReference(entity);
    }

    public void removeMemberEntityReference(EntityReference entity) {
		object.removeMemberEntityReference(entity);
    }

	public void setMemberEntityReference(Set<EntityReference> entity) {
		object.setMemberEntityReference(entity);
    }
	
	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return EntityReference.class;
	}
}

