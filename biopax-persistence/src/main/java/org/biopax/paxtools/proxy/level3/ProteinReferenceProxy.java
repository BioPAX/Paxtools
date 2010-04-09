/*
 * ProteinReferenceProxy.java
 *
 * 2008.02.27 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * Proxy for ProteinReference
 */
@Entity(name="l3proteinreference")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class ProteinReferenceProxy extends SequenceEntityReferenceProxy<ProteinReference> implements ProteinReference {
	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return ProteinReference.class;
	}
}
