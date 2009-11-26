/*
 * SequenceEntityReferenceProxy.java
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
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Proxy for SequenceEntityReference
 */
@Entity(name="l3sequenceentityreference")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class SequenceEntityReferenceProxy extends EntityReferenceProxy implements SequenceEntityReference, Serializable {
	public SequenceEntityReferenceProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return SequenceEntityReference.class;
	}

	// Property ORGANISM

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity= BioSourceProxy.class)
	@JoinColumn(name="organism_x")
	public BioSource getOrganism() {
		return ((SequenceEntityReference)object).getOrganism();
	}

	public void setOrganism(BioSource ORGANISM) {
		((SequenceEntityReference)object).setOrganism(ORGANISM);
	}

	// Property SEQUENCE

	@Basic @Column(name="sequence_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_SEQUENCE, index=Index.TOKENIZED)
	public String getSequence() {
		return ((SequenceEntityReference)object).getSequence();
	}

	public void setSequence(String SEQUENCE) {
		((SequenceEntityReference)object).setSequence(SEQUENCE);
	}
}
