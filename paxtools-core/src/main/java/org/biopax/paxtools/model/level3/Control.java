package org.biopax.paxtools.model.level3;

import java.util.Set;

public interface Control extends RestrictedInteraction
{

	// Property Controlled

	public Set<Process> getControlled();

	public void addControlled(Process controlled);

	public void removeControlled(Process controlled);

	public void setControlled(Set<Process> controlled);


	// Property Controller
	public Set<PhysicalEntity> getController();

	public void addController(PhysicalEntity controller);

	public void removeController(PhysicalEntity controller);

	public void setController(Set<PhysicalEntity> controller);


	// Property CONTROL-TYPE
	public ControlType getControlType();

	public void setControlType(ControlType controlType);


}
