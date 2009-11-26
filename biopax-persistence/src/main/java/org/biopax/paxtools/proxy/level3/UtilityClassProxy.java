/*
 * UtilityClassProxy.java
 *
 * 2007.03.15 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.UtilityClass;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import java.io.Serializable;

/**
 * Proxy for utilityClass
 */
@Entity(name="l3utilityclass")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public abstract class UtilityClassProxy extends Level3ElementProxy implements UtilityClass, Serializable {
	protected UtilityClassProxy() {
	}
}
