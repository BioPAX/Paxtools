/*
 * ProvenanceProxy.java
 *
 * 2008.02.26 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.*;
import org.hibernate.annotations.CollectionOfElements;
import org.biopax.paxtools.proxy.StringSetBridge;

import javax.persistence.*;
import javax.persistence.Entity;
import java.util.Set;

/**
 * Proxy for Provenance
 */
@Entity(name="l3provenance")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class ProvenanceProxy extends Level3ElementProxy implements Provenance {
	protected ProvenanceProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return Provenance.class;
	}

// XReferrable

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = XrefProxy.class)
	@JoinTable(name="l3entity_xref")
	public Set<Xref> getXref() {
		return ((Provenance)object).getXref();
	}

	public void addXref(Xref XREF) {
		((Provenance)object).addXref(XREF);
	}

	public void removeXref(Xref XREF) {
		((Provenance)object).removeXref(XREF);
	}

	public void setXref(Set<Xref> XREF) {
		((Provenance)object).setXref(XREF);
	}

// Named
	
	@CollectionOfElements @Column(name="name_x", columnDefinition="text")
	@FieldBridge(impl=StringSetBridge.class)
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_AVAILABILITY, index=Index.TOKENIZED)
	public Set<String> getName() {
		return ((Provenance)object).getName();
	}
	
	public void addName(String NAME_TEXT) {
		((Provenance)object).addName(NAME_TEXT);
	}
	
	public void removeName(String NAME_TEXT) {
		((Provenance)object).removeName(NAME_TEXT);
	}
	
	public void setName(Set<String> newNAME) {
		((Provenance)object).setName(newNAME);
	}
	
	@Basic @Column(name="display_name_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_NAME, index=Index.TOKENIZED)
	public String getDisplayName() {
		return ((Provenance)object).getDisplayName();
	}
	
	public void setDisplayName(String newDISPLAY_NAME) {
		((Provenance)object).setDisplayName(newDISPLAY_NAME);
	}
	
	@Basic @Column(name="standard_name_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_NAME, index=Index.TOKENIZED)
	public String getStandardName() {
		return ((Provenance)object).getStandardName();
	}
	
	public void setStandardName(String newSTANDARD_NAME) {
		((Provenance)object).setStandardName(newSTANDARD_NAME);
	}
	
}
