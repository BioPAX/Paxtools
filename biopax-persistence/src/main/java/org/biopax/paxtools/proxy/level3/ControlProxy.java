/*
 * ControlProxy.java
 *
 * 2007.12.03 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;
import java.io.Serializable;
import java.util.Set;

/**
 * Proxy for control
 */
@Entity(name="l3control")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class ControlProxy extends RestrictedInteractionAdapterProxy implements Control, Serializable {
	public ControlProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return Control.class;
	}

	// Property CONTROLLED

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity=ProcessProxy.class)
	@JoinTable(name="l3control_controlled")
	public Set<Process> getControlled() {
		return ((Control)object).getControlled();
	}

	public void addControlled(Process CONTROLLED) {
		((Control)object).addControlled(CONTROLLED);
	}

	public void removeControlled(Process CONTROLLED) {
		((Control)object).removeControlled(CONTROLLED);
	}

	public void setControlled(Set<Process> CONTROLLED) {
		((Control)object).setControlled(CONTROLLED);
	}

	// Property CONTROLLER

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity= PhysicalEntityProxy.class)
	@JoinTable(name="l3control_controller")
	public Set<PhysicalEntity> getController() {
		return ((Control)object).getController();
	}

	public void addController(PhysicalEntity CONTROLLER) {
		((Control)object).addController(CONTROLLER);
	}

	public void removeController(PhysicalEntity CONTROLLER) {
		((Control)object).removeController(CONTROLLER);
	}

	public void setController(Set<PhysicalEntity> CONTROLLER) {
		((Control)object).setController(CONTROLLER);
	}

	// Property CONTROL-TYPE

	@Basic @Enumerated @Column(name="control_type_x")
	public ControlType getControlType() {
		return ((Control)object).getControlType();
	}

	public void setControlType(ControlType CONTROL_TYPE) {
		((Control)object).setControlType(CONTROL_TYPE);
	}
}
