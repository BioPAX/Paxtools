/*
 * ProteinProxy.java
 *
 * 2007.03.16 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.protein;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Proxy for protein
 */
@Entity(name="l2protein")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class proteinProxy extends sequenceEntityProxy implements protein {
	public proteinProxy() {
	}
	@Transient
	public Class getModelInterface()
	{
		return protein.class;
	}

}


