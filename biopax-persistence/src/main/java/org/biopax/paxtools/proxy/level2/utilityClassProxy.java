/*
 * UtilityClassProxy.java
 *
 * 2007.03.15 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.utilityClass;

import javax.persistence.Entity;

/**
 * Proxy for utilityClass
 */
@Entity(name="l2utilityclass")
public abstract class utilityClassProxy extends Level2ElementProxy implements utilityClass {
	protected utilityClassProxy() {
	}
}
