/*
 * CatalysisProxy.java
 *
 * 2007.12.03 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;

import java.util.Set;

/**
 * Proxy for catalysis
 */
@Entity(name="l3catalysis")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class CatalysisProxy extends ControlProxy<Catalysis> implements Catalysis {
	// Property COFACTOR

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity= PhysicalEntityProxy.class)
	@JoinTable(name="l3catalysis_cofactor")
	public Set<PhysicalEntity> getCofactor() {
		return object.getCofactor();
	}

	public void addCofactor(PhysicalEntity COFACTOR) {
		object.addCofactor(COFACTOR);
	}

	public void removeCofactor(PhysicalEntity COFACTOR) {
		object.removeCofactor(COFACTOR);
	}

	public void setCofactor(Set<PhysicalEntity> COFACTOR) {
		object.setCofactor(COFACTOR);
	}

	// Property DIRECTION

	@Basic @Enumerated @Column(name="direction_x")
	public CatalysisDirectionType getCatalysisDirection() {
		return object.getCatalysisDirection();
	}

	public void setCatalysisDirection(CatalysisDirectionType DIRECTION) {
		object.setCatalysisDirection(DIRECTION);
	}
	
	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return Catalysis.class;
	}
}
