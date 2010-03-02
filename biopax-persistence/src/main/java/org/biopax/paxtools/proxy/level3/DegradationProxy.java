/*
 * DegradationProxy.java
 *
 * 2008.02.27 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Proxy for Degradation
 */
@Entity(name="l3degradation")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class DegradationProxy extends ConversionProxy implements Degradation {
	public DegradationProxy() {
	}

	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return Degradation.class;
	}
}
