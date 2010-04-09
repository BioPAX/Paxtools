/*
 * ComplexAssemblyProxy.java
 *
 * 2007.04.06 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.complexAssembly;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Proxy for complexAssembly
 */
@Entity(name="l2complexassembly")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class complexAssemblyProxy extends conversionProxy implements complexAssembly {
	public complexAssemblyProxy() {
	}
	@Transient
	public Class getModelInterface()
	{
		return complexAssembly.class;
	}

}
