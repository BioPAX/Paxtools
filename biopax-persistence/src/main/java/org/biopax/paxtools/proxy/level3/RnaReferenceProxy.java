/*
 * RnaReferenceProxy.java
 *
 * 2007.12.04 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Proxy for RnaReference
 */
@Entity(name="l3rnareference")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class RnaReferenceProxy extends SequenceEntityReferenceProxy implements RnaReference {
	public RnaReferenceProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return RnaReference.class;
	}
}
