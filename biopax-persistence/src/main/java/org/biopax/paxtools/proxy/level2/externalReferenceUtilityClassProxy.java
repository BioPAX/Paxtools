/*
 * ExternalReferenceUtilityClassProxy.java
 *
 * 2007.03.15 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.externalReferenceUtilityClass;

import javax.persistence.Entity;

/**
 * Proxy for externalReferenceUtilityClass
 */
@Entity(name="l2extrefutilityclass")
public abstract class externalReferenceUtilityClassProxy extends utilityClassProxy 
	implements externalReferenceUtilityClass
{
	protected externalReferenceUtilityClassProxy() {
	}
}
