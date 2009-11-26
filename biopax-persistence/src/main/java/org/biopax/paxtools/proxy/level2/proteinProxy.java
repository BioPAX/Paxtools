/*
 * ProteinProxy.java
 *
 * 2007.03.16 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.protein;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.Serializable;

/**
 * Proxy for protein
 */
@Entity(name="l2protein")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class proteinProxy extends sequenceEntityProxy implements protein, Serializable {
	public proteinProxy() {
	}
	@Transient
	public Class getModelInterface()
	{
		return protein.class;
	}

}


