/*
 * RnaProxy.java
 *
 * 2007.12.04 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.Serializable;

/**
 * Proxy for rna
 */
@Entity(name="l3rna")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class RnaProxy extends NucleicAcidProxy implements Rna, Serializable {
	public RnaProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return Rna.class;
	}

	@Transient
    public Class<? extends PhysicalEntity> getPhysicalEntityClass() {
        return Rna.class;

    }
}
