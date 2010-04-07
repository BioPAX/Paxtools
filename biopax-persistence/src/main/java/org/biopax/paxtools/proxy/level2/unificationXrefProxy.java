/*
 * UnificationXrefProxy.java
 *
 * 2007.03.16 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.unificationXref;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Proxy for unificationXref
 */
@Entity(name="l2unificationxref")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class unificationXrefProxy extends xrefProxy implements unificationXref {
	public unificationXrefProxy() {
	}
	
	@Transient
	public Class getModelInterface()
	{
		return unificationXref.class;
	}
}

