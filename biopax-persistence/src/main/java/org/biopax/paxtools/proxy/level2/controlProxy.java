/*
 * ControlProxy.java
 *
 * 2007.04.05 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.*;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import java.util.Set;

/**
 * Proxy for control
 */
@Entity(name="l2control")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class controlProxy extends physicalInteractionProxy implements control {
	public controlProxy()
	{

	}
	@Transient
	public Class getModelInterface()
	{
		return control.class;
	}



	public void addCONTROLLED(process CONTROLLED) {
		((control)object).addCONTROLLED(CONTROLLED);
	}

	public void addCONTROLLER(physicalEntityParticipant CONTROLLER) {
		((control)object).addCONTROLLER(CONTROLLER);
	}

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity=processProxy.class)
	@JoinTable(name="l2control_controlled")
	public Set<process> getCONTROLLED() {
		return ((control)object).getCONTROLLED();
	}

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity=physicalEntityParticipantProxy.class)
	@JoinTable(name="l2control_controller")
	public Set<physicalEntityParticipant> getCONTROLLER() {
		return ((control)object).getCONTROLLER();
	}

	@Basic @Enumerated @Column(name="control_type_x")
	public ControlType getCONTROL_TYPE() {
		return ((control)object).getCONTROL_TYPE();
	}

	public void removeCONTROLLED(process CONTROLLED) {
		((control)object).removeCONTROLLED(CONTROLLED);
	}

	public void removeCONTROLLER(physicalEntityParticipant CONTROLLER) {
		((control)object).removeCONTROLLER(CONTROLLER);
	}

	public void setCONTROLLED(Set<process> CONTROLLED) {
		((control)object).setCONTROLLED(CONTROLLED);
	}

	public void setCONTROLLER(Set<physicalEntityParticipant> CONTROLLER) {
		((control)object).setCONTROLLER(CONTROLLER);
	}

	public void setCONTROL_TYPE(ControlType CONTROL_TYPE) {
		((control)object).setCONTROL_TYPE(CONTROL_TYPE);
	}
}
