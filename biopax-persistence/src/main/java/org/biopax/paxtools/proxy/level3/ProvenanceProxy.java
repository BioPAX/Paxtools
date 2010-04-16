/*
 * ProvenanceProxy.java
 *
 * 2008.02.26 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.*;
import org.hibernate.annotations.CollectionOfElements;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.biopax.paxtools.proxy.StringSetBridge;

import javax.persistence.*;
import javax.persistence.Entity;

import java.util.Set;

/**
 * Proxy for Provenance
 */
@Entity(name="l3provenance")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class ProvenanceProxy extends XReferrableProxy<Provenance> implements Provenance {


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

	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return Provenance.class;
	}
}
