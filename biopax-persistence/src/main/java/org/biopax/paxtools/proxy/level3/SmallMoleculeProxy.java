/*
 * SmallMoleculeProxy.java
 *
 * 2007.12.04 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import java.io.Serializable;

/**
 * Proxy for smallMolecule
 */
@Entity(name="l3smallmolecule")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class SmallMoleculeProxy extends SimplePhysicalEntityProxy implements SmallMolecule, Serializable {
	public SmallMoleculeProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return SmallMolecule.class;
	}

	@Transient
    public Class<? extends PhysicalEntity> getPhysicalEntityClass() {
        return SmallMolecule.class;

    }
}
