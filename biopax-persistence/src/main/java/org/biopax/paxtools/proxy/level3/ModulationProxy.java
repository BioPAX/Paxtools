/*
 * ModulationProxy.java
 *
 * 2007.12.03 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Proxy for modulation
 */
@Entity(name="l3modulation")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class ModulationProxy extends ControlProxy implements Modulation {
	public ModulationProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return Modulation.class;
	}

}
