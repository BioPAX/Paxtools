/*
 * ComplexAssemblyProxy.java
 *
 * 2007.12.03 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import javax.persistence.Transient;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.Indexed;

/**
 * Proxy for complexAssembly
 */
@javax.persistence.Entity(name="l3complexassembly")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class ComplexAssemblyProxy extends ConversionProxy implements
	ComplexAssembly {
	public ComplexAssemblyProxy() {
	}

	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return ComplexAssembly.class;
	}
}
