/*
 * InteractionParticipantUtilityClassProxy.java
 *
 * 2007.09.10 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.utilityClass;

import javax.persistence.Entity;

/**
 * Proxy for utilityClass
 */

@Entity(name = "l2interactionparticipantutilityclass")
public abstract class InteractionParticipantUtilityClassProxy
	extends InteractionParticipantProxy implements utilityClass
{
	protected InteractionParticipantUtilityClassProxy()
	{
	}
}
