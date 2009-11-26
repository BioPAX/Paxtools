/*
 * PhysicalEntityProxy.java
 *
 * 2007.12.04 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.*;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

/**
 * Proxy for physicalEntity
 */
@Entity(name="l3physicalentity")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class PhysicalEntityProxy extends EntityProxy implements PhysicalEntity, Serializable {
	protected PhysicalEntityProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return PhysicalEntity.class;
	}

// Named

/*
	@CollectionOfElements @Column(name="name_x", columnDefinition="text")
	@FieldBridge(impl=StringSetBridge.class)
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_AVAILABILITY, index=Index.TOKENIZED)
	public Set<String> getName() {
		return ((PhysicalEntity)object).getName();
	}

	public void addName(String NAME_TEXT) {
		((PhysicalEntity)object).addName(NAME_TEXT);
	}

	public void removeName(String NAME_TEXT) {
		((PhysicalEntity)object).removeName(NAME_TEXT);
	}

	public void setName(Set<String> newNAME) {
		((PhysicalEntity)object).setName(newNAME);
	}
	
	@Basic @Column(name="display_name_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_NAME, index=Index.TOKENIZED)
	public String getDisplayName() {
		return ((PhysicalEntity)object).getDisplayName();
	}

	public void setDisplayName(String newDISPLAY_NAME) {
		((PhysicalEntity)object).setDisplayName(newDISPLAY_NAME);
	}

	@Basic @Column(name="standard_name_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_NAME, index=Index.TOKENIZED)
	public String getStandardName() {
		return ((PhysicalEntity)object).getStandardName();
	}

	public void setStandardName(String newSTANDARD_NAME) {
		((PhysicalEntity)object).setStandardName(newSTANDARD_NAME);
	}
*/

// Observable

/*
	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = EvidenceProxy.class)
	@JoinTable(name="l3physicalentity_evidence")
	public Set<Evidence> getEvidence() {
		return ((PhysicalEntity)object).getEvidence();
	}

	public void addEVIDENCE(Evidence newEvidence) {
		((PhysicalEntity)object).addEVIDENCE(newEvidence);
	}

	public void removeEVIDENCE(Evidence oldEvidence) {
		((PhysicalEntity)object).removeEVIDENCE(oldEvidence);
	}

	public void setEvidence(Set<Evidence> newEvidence) {
		((PhysicalEntity)object).setEvidence(newEvidence);
	}
*/

// physicalEntity

	// Property BINDS-TO
/*
	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = PhysicalEntityProxy.class)
	@JoinTable(name="l3physicalentity_binds_to")
	public Set<PhysicalEntity> getBoundTo() {
		return ((PhysicalEntity)object).getBoundTo();
	}

	public void addBoundTo(PhysicalEntity newBINDS_TO) {
		((PhysicalEntity)object).addBoundTo(newBINDS_TO);
	}

	public void removeBoundTo(PhysicalEntity oldBINDS_TO) {
		((PhysicalEntity)object).removeBoundTo(oldBINDS_TO);
	}

	public void setBoundTo(Set<PhysicalEntity> newBINDS_TO) {
		((PhysicalEntity)object).setBoundTo(newBINDS_TO);
	}
*/

	// Inverse of COMPONENT

//	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity = ComplexProxy.class)
//	@JoinColumn(name="component_of_x")
//	public Complex getComponentOf() {
//		return ((PhysicalEntity)object).getComponentOf();
//	}

	@Transient
	public Set<Complex> getComponentOf() {
		return ((PhysicalEntity)object).getComponentOf();
	}
/*
 @ManyToOne(cascade = {CascadeType.ALL}, targetEntity = ComplexProxy.class)
	@JoinColumn(name="component_of_x")
	public Complex isComponentOf() {
		return ((PhysicalEntity)object).isComponentOf();
		//return getComponentOf();
	}

	public void setComponentOf(Complex newComplex) {
		((PhysicalEntity)object).setComponentOf(newComplex);
	}
*/

	// Property CELLULAR-LOCATION

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity = CellularLocationVocabularyProxy.class)
	@JoinColumn(name="cellular_location_x")
	public CellularLocationVocabulary getCellularLocation() {
		return ((PhysicalEntity)object).getCellularLocation();
	}

	public void setCellularLocation(CellularLocationVocabulary newCELLULAR_LOCATION) {
		((PhysicalEntity)object).setCellularLocation(newCELLULAR_LOCATION);
	}

	// Property MODIFIED-AT

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = EntityFeatureProxy.class)
	@JoinTable(name="l3physicalentity_mod_at")
	public Set<EntityFeature> getFeature() {
		return ((PhysicalEntity)object).getFeature();
	}

	public void addFeature(EntityFeature newMODIFIED_AT) {
		((PhysicalEntity)object).addFeature(newMODIFIED_AT);
	}

	public void removeFeature(EntityFeature oldMODIFIED_AT) {
		((PhysicalEntity)object).removeFeature(oldMODIFIED_AT);
	}

	public void setFeature(Set<EntityFeature> newMODIFIED_AT) {
		((PhysicalEntity)object).setFeature(newMODIFIED_AT);
	}

	// Property NOT-MODIFIED-AT

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = EntityFeatureProxy.class)
	@JoinTable(name="l3physicalentity_not_mod_at")
	public Set<EntityFeature> getNotFeature() {
		return ((PhysicalEntity)object).getNotFeature();
	}

	public void addNotFeature(EntityFeature newNOT_MODIFIED_AT) {
		((PhysicalEntity)object).addNotFeature(newNOT_MODIFIED_AT);
	}

	public void removeNotFeature(EntityFeature oldNOT_MODIFIED_AT) {
		((PhysicalEntity)object).removeNotFeature(oldNOT_MODIFIED_AT);
	}

	public void setNotFeature(Set<EntityFeature> newNOT_MODIFIED_AT) {
		((PhysicalEntity)object).setNotFeature(newNOT_MODIFIED_AT);
	}

	//Property memberPhysicalEntity

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = PhysicalEntityProxy.class)
	@JoinTable(name="l3physicalentity_member_pe")
	public Set<PhysicalEntity> getMemberPhysicalEntity() {
		return ((PhysicalEntity)object).getMemberPhysicalEntity();
	}

	public void addMemberPhysicalEntity(PhysicalEntity memberPhysicalEntity) {
		((PhysicalEntity)object).addMemberPhysicalEntity(memberPhysicalEntity);
	}

	public void removeMemberPhysicalEntity(PhysicalEntity memberPhysicalEntity) {
		((PhysicalEntity)object).removeMemberPhysicalEntity(memberPhysicalEntity);
	}

	public void setMemberPhysicalEntity(Set<PhysicalEntity> memberPhysicalEntity) {
		((PhysicalEntity)object).setMemberPhysicalEntity(memberPhysicalEntity);
	}

	@Transient
    public Class<? extends PhysicalEntity> getPhysicalEntityClass() {
        return PhysicalEntity.class;

    }

	public boolean hasEquivalentCellularLocation(PhysicalEntity that) {
		return ((PhysicalEntity)object).hasEquivalentCellularLocation(that);
	}

	public boolean hasEquivalentFeatures(PhysicalEntity that) {
		return ((PhysicalEntity)object).hasEquivalentFeatures(that);
	}

/*
	// Property REFERENCE-ENTITY

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity = EntityReferenceProxy.class)
	@JoinColumn(name="entity_reference_x")
	public EntityReference getEntityReference() {
		return ((PhysicalEntity)object).getEntityReference();
	}

	public void setEntityReference(EntityReference newReference_ENTITY) {
		((PhysicalEntity)object).setEntityReference(newReference_ENTITY);
	}
*/
}

