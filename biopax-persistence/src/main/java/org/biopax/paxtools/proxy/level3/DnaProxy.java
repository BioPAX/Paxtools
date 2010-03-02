/*
 * DnaProxy.java
 *
 * 2007.12.03 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Dna;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Proxy for dna
 */
@Entity(name="l3dna")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class DnaProxy extends SimplePhysicalEntityProxy implements Dna {
	public DnaProxy() {
	}

	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return Dna.class;
	}
}
