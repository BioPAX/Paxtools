/*
 * RnaProxy.java
 *
 * 2007.04.05 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.rna;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.Serializable;

/**
 * Proxy for dna
 */
@Entity(name="l2rna")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class rnaProxy extends sequenceEntityProxy implements rna, Serializable {
	public rnaProxy() {
	}
	@Transient
	public Class getModelInterface()
	{
		return rna.class;
	}

}

