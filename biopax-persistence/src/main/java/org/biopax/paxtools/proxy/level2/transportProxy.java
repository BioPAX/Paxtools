/*
 * TransportProxy.java
 *
 * 2007.04.06 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.transport;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.Serializable;

/**
 * Proxy for transport
 */
@Entity(name="l2transport")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class transportProxy extends conversionProxy implements transport, Serializable {
	public transportProxy() {
	}
	@Transient
	public Class getModelInterface()
	{
		return transport.class;
	}

}
