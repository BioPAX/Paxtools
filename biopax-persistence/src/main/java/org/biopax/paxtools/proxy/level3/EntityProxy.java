/*
 * EntityProxy.java
 *
 * 2007.11.30 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Entity;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;
import org.biopax.paxtools.proxy.StringSetBridge;

/**
 * Proxy for entity
 */
@javax.persistence.Entity(name="l3entity")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public abstract class EntityProxy extends Level3ElementProxy implements Entity, Serializable {
	
	protected EntityProxy() {
		// not get object. because this object has not factory.
	}

// XReferrable

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = XrefProxy.class)
	@JoinTable(name="l3entity_xref")
	public Set<Xref> getXref() {
		return ((Entity)object).getXref();
	}

	public void addXref(Xref XREF) {
		((Entity)object).addXref(XREF);
	}

	public void removeXref(Xref XREF) {
		((Entity)object).removeXref(XREF);
	}

	public void setXref(Set<Xref> XREF) {
		((Entity)object).setXref(XREF);
	}

// Observable

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = EvidenceProxy.class)
	@JoinTable(name="l3entity_evidence")
	public Set<Evidence> getEvidence() {
		return ((Entity)object).getEvidence();
	}

	public void addEvidence(Evidence newEvidence) {
		((Entity)object).addEvidence(newEvidence);
	}

	public void removeEvidence(Evidence oldEvidence) {
		((Entity)object).removeEvidence(oldEvidence);
	}

	public void setEvidence(Set<Evidence> newEvidence) {
		((Entity)object).setEvidence(newEvidence);
	}

// Named
	
	@CollectionOfElements @Column(name="name_x", columnDefinition="text")
	@FieldBridge(impl=StringSetBridge.class)
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_AVAILABILITY, index=Index.TOKENIZED)
	public Set<String> getName() {
		return ((Entity)object).getName();
	}
	
	public void addName(String NAME_TEXT) {
		((Entity)object).addName(NAME_TEXT);
	}
	
	public void removeName(String NAME_TEXT) {
		((Entity)object).removeName(NAME_TEXT);
	}
	
	public void setName(Set<String> newNAME) {
		((Entity)object).setName(newNAME);
	}
	
	@Basic @Column(name="display_name_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_NAME, index=Index.TOKENIZED)
	public String getDisplayName() {
		return ((Entity)object).getDisplayName();
	}
	
	public void setDisplayName(String newDISPLAY_NAME) {
		((Entity)object).setDisplayName(newDISPLAY_NAME);
	}
	
	@Basic @Column(name="standard_name_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_NAME, index=Index.TOKENIZED)
	public String getStandardName() {
		return ((Entity)object).getStandardName();
	}
	
	public void setStandardName(String newSTANDARD_NAME) {
		((Entity)object).setStandardName(newSTANDARD_NAME);
	}

// entity

	// Property AVAILABILITY

	@CollectionOfElements @Column(name="availability_x", columnDefinition="text")
	@FieldBridge(impl=StringSetBridge.class)
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_AVAILABILITY, index=Index.TOKENIZED)
	public Set<String> getAvailability() {
		return ((Entity)object).getAvailability();
	}

	public void addAvailability(String AVAILABILITY_TEXT) {
		((Entity)object).addAvailability(AVAILABILITY_TEXT);
	}
	
	public void removeAvailability(String AVAILABILITY_TEXT) {
		((Entity)object).removeAvailability(AVAILABILITY_TEXT);
	}
	
	public void setAvailability(Set<String> AVAILABILITY_TEXT) {
		((Entity)object).setAvailability(AVAILABILITY_TEXT);
	}
	
	// Property DATA-SOURCE

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = ProvenanceProxy.class)
	@JoinTable(name="l3entity_data_source")
	public Set<Provenance> getDataSource() {
		return ((Entity)object).getDataSource();
	}
	
	public void addDataSource(Provenance DATA_SOURCE_INST) {
		((Entity)object).addDataSource(DATA_SOURCE_INST);
	}

	public void removeDataSource(Provenance DATA_SOURCE_INST) {
		((Entity)object).removeDataSource(DATA_SOURCE_INST);
	}

	public void setDataSource(Set<Provenance> DATA_SOURCE) {
		((Entity)object).setDataSource(DATA_SOURCE);
	}
	
	// Inverse method of PARTICIPANT

	@Transient
	public Set<Interaction> getParticipantsOf() {
		return ((Entity)object).getParticipantsOf();
	}
}

