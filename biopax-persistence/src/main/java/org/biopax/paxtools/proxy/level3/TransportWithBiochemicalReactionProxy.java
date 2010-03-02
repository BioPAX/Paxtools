/*
 * TransportWithBiochemicalReactionProxy.java
 *
 * 2007.12.04 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Proxy for transportWithBiochemicalReaction
 */
@Entity(name="l3transportwithbioreact")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class TransportWithBiochemicalReactionProxy extends BiochemicalReactionProxy 
	implements	TransportWithBiochemicalReaction 
{
	public TransportWithBiochemicalReactionProxy() {
	}
	
	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return TransportWithBiochemicalReaction.class;
	}
}
