/*
 * SimplePhysicalEntityProxy.java
 *
 * 2008.06.05 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;

/**
 * Proxy for SimplePhysicalEntityProxy
 */
@Entity(name="l3simplephysicalentity")
//@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public abstract class SimplePhysicalEntityProxy extends PhysicalEntityProxy 
	implements SimplePhysicalEntity 
{
	SimplePhysicalEntityProxy() {
	}

	// Property REFERENCE-ENTITY

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity = EntityReferenceProxy.class)
	@JoinColumn(name="entity_reference_x")
	public EntityReference getEntityReference() {
		return ((SimplePhysicalEntity)object).getEntityReference();
	}

	public void setEntityReference(EntityReference entityReference) {
		((SimplePhysicalEntity)object).setEntityReference(entityReference);
	}
}
