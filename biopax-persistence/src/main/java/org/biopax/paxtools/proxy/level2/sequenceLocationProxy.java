/*
 * SequenceLocationProxy.java
 *
 * 2007.04.02 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.sequenceLocation;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import java.io.Serializable;

/**
 * Proxy for sequenceLocation
 */
@Entity(name="l2sequencelocation")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public abstract class sequenceLocationProxy extends utilityClassProxy implements sequenceLocation, Serializable {
	protected sequenceLocationProxy() {
	}
}
