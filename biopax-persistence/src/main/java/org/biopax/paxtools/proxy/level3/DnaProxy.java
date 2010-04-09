/*
 * DnaProxy.java
 *
 * 2007.12.03 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.Dna;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Proxy for dna
 */
@Entity(name="l3dna")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class DnaProxy extends SimplePhysicalEntityProxy<Dna> implements Dna {

	@Transient
	public Class<? extends PhysicalEntity> getModelInterface() {
		return Dna.class;
	}
}
