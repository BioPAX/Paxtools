/*
 * GeneProxy.java
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


/**
 * Proxy for Gene
 */
@Entity(name="l3gene")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class GeneProxy extends EntityProxy implements Gene {
	// Property ORGANISM

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity= BioSourceProxy.class)
	@JoinColumn(name="organism_x")
	public BioSource getOrganism() {
		return ((Gene)object).getOrganism();
	}

	public void setOrganism(BioSource ORGANISM) {
		((Gene)object).setOrganism(ORGANISM);
	}
	
	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return Gene.class;
	}
}
