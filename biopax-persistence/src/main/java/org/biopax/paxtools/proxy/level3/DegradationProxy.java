/*
 * DegradationProxy.java
 *
 * 2008.02.27 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.Serializable;

/**
 * Proxy for Degradation
 */
@Entity(name="l3degradation")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class DegradationProxy extends ConversionProxy implements Degradation, Serializable {
	public DegradationProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return Degradation.class;
	}
}
