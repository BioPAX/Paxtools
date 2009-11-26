/*
 * ExternalReferenceUtilityClassProxy.java
 *
 * 2007.03.15 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.externalReferenceUtilityClass;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import java.io.Serializable;

/**
 * Proxy for externalReferenceUtilityClass
 */
@Entity(name="l2extrefutilityclass")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public abstract class externalReferenceUtilityClassProxy extends utilityClassProxy implements externalReferenceUtilityClass, Serializable {
	protected externalReferenceUtilityClassProxy() {
	}
}
