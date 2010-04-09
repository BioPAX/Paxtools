/*
 * ModulationProxy.java
 *
 * 2007.04.06 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.modulation;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Proxy for modulation
 */
@Entity(name="l2modulation")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class modulationProxy extends controlProxy implements modulation {
	public modulationProxy() {
	}
	@Transient
	public Class getModelInterface()
	{
		return modulation.class;
	}

}
