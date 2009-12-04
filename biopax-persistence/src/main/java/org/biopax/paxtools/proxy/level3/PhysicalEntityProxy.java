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
import javax.persistence.*;

import java.util.Set;

/**
 * Proxy for physicalEntity
 */
@Entity(name="l3physicalentity")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class PhysicalEntityProxy extends EntityProxy implements PhysicalEntity {
	protected PhysicalEntityProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return PhysicalEntity.class;
	}

	@Transient
	public Set<Complex> getComponentOf() {
		return ((PhysicalEntity)object).getComponentOf();
	}

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
        return (Class<? extends PhysicalEntity>) 
        	((PhysicalEntity)object).getModelInterface();
    }

	public boolean hasEquivalentCellularLocation(PhysicalEntity that) {
		return ((PhysicalEntity)object).hasEquivalentCellularLocation(that);
	}

	public boolean hasEquivalentFeatures(PhysicalEntity that) {
		return ((PhysicalEntity)object).hasEquivalentFeatures(that);
	}

}

