/*
 * GeneProxy.java
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
 * Proxy for Gene
 */
@Entity(name="l3gene")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class GeneProxy extends EntityProxy implements Gene, Serializable {
	public GeneProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return Gene.class;
	}

	// Property ORGANISM

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity= BioSourceProxy.class)
	@JoinColumn(name="organism_x")
	public BioSource getOrganism() {
		return ((Gene)object).getOrganism();
	}

	public void setOrganism(BioSource ORGANISM) {
		((Gene)object).setOrganism(ORGANISM);
	}
}
