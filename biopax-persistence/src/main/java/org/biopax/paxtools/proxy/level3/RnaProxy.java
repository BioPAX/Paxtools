/*
 * RnaProxy.java
 *
 * 2007.12.04 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import javax.persistence.Transient;

import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.Indexed;

/**
 * Proxy for rna
 */
@javax.persistence.Entity(name="l3rna")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class RnaProxy extends SimplePhysicalEntityProxy<Rna> implements Rna
{
	
	@Transient
	public Class<? extends PhysicalEntity> getModelInterface() {
		return Rna.class;
	}
}
