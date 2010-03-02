/*
 * RnaProxy.java
 *
 * 2007.12.04 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import javax.persistence.Transient;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.Indexed;

/**
 * Proxy for rna
 */
@javax.persistence.Entity(name="l3rna")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class RnaProxy extends SimplePhysicalEntityProxy implements Rna 
{
	public RnaProxy() {
	}
	
	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return Rna.class;
	}
}
