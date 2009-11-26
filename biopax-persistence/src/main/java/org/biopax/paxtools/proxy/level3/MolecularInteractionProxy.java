/*
 * physicalInteractionProxy.java
 *
 * 2007.12.04 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;
import java.io.Serializable;

/**
 * Proxy for physicalInteraction
 */
@Entity(name="l3physicalinteraction")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class MolecularInteractionProxy extends InteractionProxy implements MolecularInteraction, Serializable {
	public MolecularInteractionProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return MolecularInteraction.class;
	}
}
