/*
 * TransportWithBiochemicalReactionProxy.java
 *
 * 2007.04.06 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.transportWithBiochemicalReaction;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.Serializable;

/**
 * Proxy for transportWithBiochemicalReaction
 */
@Entity(name="l2transportwithbioreact")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class transportWithBiochemicalReactionProxy extends biochemicalReactionProxy implements transportWithBiochemicalReaction, Serializable {
	public transportWithBiochemicalReactionProxy() {
	}
	@Transient
	public Class getModelInterface()
	{
		return transportWithBiochemicalReaction.class;
	}

}
