/*
 * InteractionParticipantUtilityClassProxy.java
 *
 * 2007.09.10 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.utilityClass;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import java.io.Serializable;

/**
 * Proxy for utilityClass
 */

@Entity(name = "l2interactionparticipantutilityclass")
@Indexed(index = BioPAXElementProxy.SEARCH_INDEX_NAME)
public abstract class InteractionParticipantUtilityClassProxy
	extends InteractionParticipantProxy implements utilityClass, Serializable
{
	protected InteractionParticipantUtilityClassProxy()
	{
	}
}
