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

/**
 * Proxy for rna
 */
@Entity(name="l3rna")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class RnaProxy extends SimplePhysicalEntityProxy implements Rna 
{
	public RnaProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return Rna.class;
	}

}
