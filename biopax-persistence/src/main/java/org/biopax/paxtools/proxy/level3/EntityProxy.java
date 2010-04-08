/*
 * EntityProxy.java
 *
 * 2007.11.30 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.search.annotations.*;

import java.util.Set;

import javax.persistence.*;

import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.biopax.paxtools.proxy.StringSetBridge;
import org.biopax.paxtools.model.level3.Entity;

/**
 * Proxy for entity
 */
@javax.persistence.Entity(name="l3entity")
public abstract class EntityProxy<T extends Entity> extends Level3ElementProxy<T> implements Entity {
	
// XReferrable

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = XrefProxy.class)
	@JoinTable(name="l3entity_xref")
	public Set<Xref> getXref() {
		return object.getXref();
	}

	public void addXref(Xref XREF) {
		object.addXref(XREF);
	}

	public void removeXref(Xref XREF) {
		object.removeXref(XREF);
	}

	public void setXref(Set<Xref> XREF) {
		object.setXref(XREF);
	}

// Observable

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = EvidenceProxy.class)
	@JoinTable(name="l3entity_evidence")
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

// Named
	
	@CollectionOfElements @Column(name="name_x", columnDefinition="text")
	@FieldBridge(impl=StringSetBridge.class)
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_AVAILABILITY, index=Index.TOKENIZED)
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

// entity

	// Property AVAILABILITY

	@CollectionOfElements @Column(name="availability_x", columnDefinition="text")
	@FieldBridge(impl=StringSetBridge.class)
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_AVAILABILITY, index=Index.TOKENIZED)
	public Set<String> getAvailability() {
		return object.getAvailability();
	}

	public void addAvailability(String AVAILABILITY_TEXT) {
		object.addAvailability(AVAILABILITY_TEXT);
	}
	
	public void removeAvailability(String AVAILABILITY_TEXT) {
		object.removeAvailability(AVAILABILITY_TEXT);
	}
	
	public void setAvailability(Set<String> AVAILABILITY_TEXT) {
		object.setAvailability(AVAILABILITY_TEXT);
	}
	
	// Property DATA-SOURCE

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = ProvenanceProxy.class)
	@JoinTable(name="l3entity_data_source")
	public Set<Provenance> getDataSource() {
		return object.getDataSource();
	}
	
	public void addDataSource(Provenance DATA_SOURCE_INST) {
		object.addDataSource(DATA_SOURCE_INST);
	}

	public void removeDataSource(Provenance DATA_SOURCE_INST) {
		object.removeDataSource(DATA_SOURCE_INST);
	}

	public void setDataSource(Set<Provenance> DATA_SOURCE) {
		object.setDataSource(DATA_SOURCE);
	}
	
	// Inverse method of PARTICIPANT

	@Transient
	public Set<Interaction> getParticipantsOf() {
		return object.getParticipantsOf();
	}
	
}

