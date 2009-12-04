/*
 * DnaReferenceProxy.java
 *
 * 2007.12.04 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Proxy for DnaReference
 */
@Entity(name="l3dnareference")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class DnaReferenceProxy extends SequenceEntityReferenceProxy implements DnaReference {
	public DnaReferenceProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return DnaReference.class;
	}

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity = SequenceIntervalProxy.class)
	@JoinColumn(name="genomic_region_x")
	public SequenceInterval getGenomicRegion() {
		return ((DnaReference)object).getGenomicRegion();
	}

	public void setGenomicRegion(SequenceInterval genomicRegion) {
		((DnaReference)object).setGenomicRegion(genomicRegion);
	}
}
