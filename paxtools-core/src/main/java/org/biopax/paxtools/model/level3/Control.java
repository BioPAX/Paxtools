package org.biopax.paxtools.model.level3;

import java.util.Set;

public interface Control extends Interaction {

	// Property Controlled

	public Set<Process> getControlled();

	public void addControlled(Process controlled);

	public void removeControlled(Process controlled);




	// Property Controller
	public Set<Controller> getController();

	public void addController(Controller controller);

	public void removeController(Controller controller);




	// Property CONTROL-TYPE
	public ControlType getControlType();

	public void setControlType(ControlType controlType);


}
