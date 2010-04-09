/*
 * ControlProxy.java
 *
 * 2007.12.03 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;

import java.util.Set;

/**
 * Proxy for control
 */
@Entity(name="l3control")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class ControlProxy<T extends Control> extends InteractionProxy<T> implements Control {
	// Property CONTROLLED

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity=ProcessProxy.class)
	@JoinTable(name="l3control_controlled")
	public Set<Process> getControlled() {
		return object.getControlled();
	}

	public void addControlled(Process CONTROLLED) {
		object.addControlled(CONTROLLED);
	}

	public void removeControlled(Process CONTROLLED) {
		object.removeControlled(CONTROLLED);
	}

	public void setControlled(Set<Process> CONTROLLED) {
		object.setControlled(CONTROLLED);
	}

	// Property CONTROLLER

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity= PhysicalEntityProxy.class)
	@JoinTable(name="l3control_controller")
	public Set<Controller> getController() {
		return object.getController();
	}

	public void addController(Controller CONTROLLER) {
		object.addController(CONTROLLER);
	}

	public void removeController(Controller CONTROLLER) {
		object.removeController(CONTROLLER);
	}

	public void setController(Set<Controller> CONTROLLER) {
		object.setController(CONTROLLER);
	}

	// Property CONTROL-TYPE

	@Basic @Enumerated @Column(name="control_type_x")
	public ControlType getControlType() {
		return object.getControlType();
	}

	public void setControlType(ControlType CONTROL_TYPE) {
		object.setControlType(CONTROL_TYPE);
	}
	
	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return Control.class;
	}
}
