/*
 * ComplexAssemblyProxy.java
 *
 * 2007.12.03 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.Serializable;

/**
 * Proxy for complexAssembly
 */
@Entity(name="l3complexassembly")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class ComplexAssemblyProxy extends ConversionProxy implements
	ComplexAssembly, Serializable {
	public ComplexAssemblyProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return ComplexAssembly.class;
	}

}
