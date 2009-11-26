/*
 * UtilityClassProxy.java
 *
 * 2007.03.15 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.utilityClass;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import java.io.Serializable;

/**
 * Proxy for utilityClass
 */
@Entity(name="l2utilityclass")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public abstract class utilityClassProxy extends Level2ElementProxy implements utilityClass, Serializable {
	protected utilityClassProxy() {
	}
}
