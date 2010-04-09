/*
 * SmallMoleculeProxy.java
 *
 * 2007.12.04 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.*;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Proxy for smallMolecule
 */
@Entity(name="l3smallmolecule")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class SmallMoleculeProxy extends SimplePhysicalEntityProxy 
	implements SmallMolecule 
{
	
	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return SmallMolecule.class;
	}
}
