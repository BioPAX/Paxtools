/*
 * pathwayComponentProxy.java
 *
 * 2007.04.05 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.pathway;
import org.biopax.paxtools.model.level2.pathwayComponent;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.Set;

/**
 * Proxy for pathwayComponent
 */
@Entity(name="l2pathwaycomponent")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public abstract class pathwayComponentProxy extends Level2ElementProxy implements pathwayComponent, Serializable {
	protected pathwayComponentProxy() {
	}

	@Transient
	public Set<pathway> isPATHWAY_COMPONENTSof() {
		return ((pathwayComponent)object).isPATHWAY_COMPONENTSof();
	}
}

