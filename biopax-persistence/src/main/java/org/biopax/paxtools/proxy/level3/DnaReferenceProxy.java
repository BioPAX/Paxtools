/*
 * DnaReferenceProxy.java
 *
 * 2007.12.04 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 * Proxy for DnaReference
 */
@Entity(name="l3dnareference")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class DnaReferenceProxy extends SequenceEntityReferenceProxy<DnaReference> implements DnaReference {

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity = SequenceIntervalProxy.class)
	@JoinColumn(name="genomic_region_x")
	public SequenceInterval getGenomicRegion() {
		return object.getGenomicRegion();
	}

	public void setGenomicRegion(SequenceInterval genomicRegion) {
		object.setGenomicRegion(genomicRegion);
	}
	
	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return DnaReference.class;
	}
}
