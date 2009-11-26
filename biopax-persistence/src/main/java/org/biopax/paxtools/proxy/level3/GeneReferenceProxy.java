/*
 * GeneReferenceProxy.java
 *
 * 2008.02.27 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.Serializable;

/**
 * Proxy for DnaReference
 */
@Entity(name="l3genereference")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class GeneReferenceProxy extends EntityReferenceProxy implements GeneReference, Serializable {
	public GeneReferenceProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return GeneReference.class;
	}
}
