/*
 * NucleicAcidProxy.java
 *
 * 2008.06.05 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.*;
import javax.persistence.Entity;
import java.io.Serializable;

/**
 * Proxy for NucleicAcid
 */
@Entity(name="l3nucleicacid")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public abstract class NucleicAcidProxy extends SimplePhysicalEntityProxy implements NucleicAcid, Serializable {
	
	protected NucleicAcidProxy() {
		// not get object. because this object has not factory.
	}

}
