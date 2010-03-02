/*
 * physicalInteractionProxy.java
 *
 * 2007.12.04 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import javax.persistence.Transient;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.Indexed;


/**
 * Proxy for physicalInteraction
 */
@javax.persistence.Entity(name="l3physicalinteraction")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class MolecularInteractionProxy extends InteractionProxy implements MolecularInteraction {
	public MolecularInteractionProxy() {
	}
	
	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return MolecularInteraction.class;
	}
}
