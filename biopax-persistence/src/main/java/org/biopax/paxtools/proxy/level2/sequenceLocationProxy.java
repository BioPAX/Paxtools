/*
 * SequenceLocationProxy.java
 *
 * 2007.04.02 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.sequenceLocation;

import javax.persistence.Entity;

/**
 * Proxy for sequenceLocation
 */
@Entity(name="l2sequencelocation")
public abstract class sequenceLocationProxy extends utilityClassProxy implements sequenceLocation {
	protected sequenceLocationProxy() {
	}
}
