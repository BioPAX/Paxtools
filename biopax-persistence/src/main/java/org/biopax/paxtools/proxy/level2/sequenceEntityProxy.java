/*
 * sequenceEntityProxy.java
 *
 * 2007.03.16 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.bioSource;
import org.biopax.paxtools.model.level2.sequenceEntity;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.*;

import javax.persistence.*;

/**
 * Proxy for sequenceEntity
 */
@Entity(name="l2sequenceentity")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public abstract class sequenceEntityProxy extends physicalEntityProxy implements sequenceEntity {
	protected sequenceEntityProxy() {
		// not get object. because this object has not factory.
	}

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity=bioSourceProxy.class)
	@JoinColumn(name="organism_x")
	public bioSource getORGANISM() {
		return ((sequenceEntity)object).getORGANISM();
	}

	@Basic @Column(name="sequence_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_SEQUENCE, index=Index.TOKENIZED)
	public String getSEQUENCE() {
		return ((sequenceEntity)object).getSEQUENCE();
	}

	public void setORGANISM(bioSource ORGANISM) {
		((sequenceEntity)object).setORGANISM(ORGANISM);
	}

	public void setSEQUENCE(String SEQUENCE) {
		((sequenceEntity)object).setSEQUENCE(SEQUENCE);
	}
}

