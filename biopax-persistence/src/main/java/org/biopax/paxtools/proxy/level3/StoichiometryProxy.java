/*
 * StoichiometryProxy.java
 *
 * 2007.12.03 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import java.io.Serializable;

/**
 * Proxy for Stoichiometry
 */
@Entity(name="l3stoichiometry")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class StoichiometryProxy extends UtilityClassProxy implements Stoichiometry, Serializable {
	public StoichiometryProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return Stoichiometry.class;
	}

	// Property PHYSICAL-ENTITY

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity = PhysicalEntityProxy.class)
	@JoinColumn(name="physical_entity_x")
	public PhysicalEntity getPhysicalEntity() {
		return ((Stoichiometry)object).getPhysicalEntity();
	}

	public void setPhysicalEntity(PhysicalEntity newPhysical_ENTITY) {
		((Stoichiometry)object).setPhysicalEntity(newPhysical_ENTITY);
	}

	// Property STOICHIOMETRIC-COEFFICIENT

	@Basic @Column(name="stoichiometric_coefficient_x", columnDefinition="text")
	protected String getStoichiometricCoefficient_x() {
		return floatToString(((Stoichiometry)object).getStoichiometricCoefficient());
	}

	protected void setStoichiometricCoefficient_x(String newSTOICHIOMETRIC_COEFFICIENT) {
		((Stoichiometry)object).setStoichiometricCoefficient(stringToFloat(newSTOICHIOMETRIC_COEFFICIENT));
	}

	@Transient
	public float getStoichiometricCoefficient() {
		return stringToFloat(getStoichiometricCoefficient_x());
	}

	public void setStoichiometricCoefficient(float newSTOICHIOMETRIC_COEFFICIENT) {
		setStoichiometricCoefficient_x(floatToString(newSTOICHIOMETRIC_COEFFICIENT));
	}
}

