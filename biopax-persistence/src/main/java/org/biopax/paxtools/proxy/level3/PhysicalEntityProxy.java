/*
 * PhysicalEntityProxy.java
 *
 * 2007.12.04 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.*;

import javax.persistence.Entity;
import javax.persistence.*;

import java.util.Set;

/**
 * Proxy for physicalEntity
 */
@Entity(name="l3physicalentity")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class PhysicalEntityProxy<T extends PhysicalEntity> extends EntityProxy<T> implements PhysicalEntity {

	@Transient
	public Set<Complex> getComponentOf() {
		return object.getComponentOf();
	}

	// Property CELLULAR-LOCATION

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity = CellularLocationVocabularyProxy.class)
	@JoinColumn(name="cellular_location_x")
	public CellularLocationVocabulary getCellularLocation() {
		return object.getCellularLocation();
	}

	public void setCellularLocation(CellularLocationVocabulary newCELLULAR_LOCATION) {
		object.setCellularLocation(newCELLULAR_LOCATION);
	}

	// Property MODIFIED-AT

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = EntityFeatureProxy.class)
	@JoinTable(name="l3physicalentity_mod_at")
	public Set<EntityFeature> getFeature() {
		return object.getFeature();
	}

	public void addFeature(EntityFeature newMODIFIED_AT) {
		object.addFeature(newMODIFIED_AT);
	}

	public void removeFeature(EntityFeature oldMODIFIED_AT) {
		object.removeFeature(oldMODIFIED_AT);
	}

	public void setFeature(Set<EntityFeature> newMODIFIED_AT) {
		object.setFeature(newMODIFIED_AT);
	}

	// Property NOT-MODIFIED-AT

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = EntityFeatureProxy.class)
	@JoinTable(name="l3physicalentity_not_mod_at")
	public Set<EntityFeature> getNotFeature() {
		return object.getNotFeature();
	}

	public void addNotFeature(EntityFeature newNOT_MODIFIED_AT) {
		object.addNotFeature(newNOT_MODIFIED_AT);
	}

	public void removeNotFeature(EntityFeature oldNOT_MODIFIED_AT) {
		object.removeNotFeature(oldNOT_MODIFIED_AT);
	}

	public void setNotFeature(Set<EntityFeature> newNOT_MODIFIED_AT) {
		object.setNotFeature(newNOT_MODIFIED_AT);
	}

	//Property memberPhysicalEntity

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = PhysicalEntityProxy.class)
	@JoinTable(name="l3physicalentity_member_pe")
	public Set<PhysicalEntity> getMemberPhysicalEntity() {
		return object.getMemberPhysicalEntity();
	}

	public void addMemberPhysicalEntity(PhysicalEntity memberPhysicalEntity) {
		object.addMemberPhysicalEntity(memberPhysicalEntity);
	}

	public void removeMemberPhysicalEntity(PhysicalEntity memberPhysicalEntity) {
		object.removeMemberPhysicalEntity(memberPhysicalEntity);
	}

	public void setMemberPhysicalEntity(Set<PhysicalEntity> memberPhysicalEntity) {
		object.setMemberPhysicalEntity(memberPhysicalEntity);
	}

    @ManyToMany(targetEntity = PhysicalEntityProxy.class, mappedBy="memberPhysicalEntity")
    public Set<PhysicalEntity> getMemberPhysicalEntityOf()
	{
		return object.getMemberPhysicalEntityOf();
	}

    private void setMemberPhysicalEntityOf(Set<PhysicalEntity> newSet)
	{
		updateSet(object.getMemberPhysicalEntityOf(),newSet);
	}

	public boolean hasEquivalentCellularLocation(PhysicalEntity that) {
		return object.hasEquivalentCellularLocation(that);
	}

	public boolean hasEquivalentFeatures(PhysicalEntity that) {
		return object.hasEquivalentFeatures(that);
	}
	
	@Transient
	public Class<? extends PhysicalEntity> getModelInterface() {
		return PhysicalEntity.class;
	}

@Transient
	public Set<Control> getControllerOf()
	{
		return object.getControllerOf();
	}}

