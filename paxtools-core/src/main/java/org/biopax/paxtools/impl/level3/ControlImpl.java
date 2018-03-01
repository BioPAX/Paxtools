package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.util.BPCollections;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


public class  ControlImpl extends InteractionImpl
		implements Control
{
// ------------------------------ FIELDS ------------------------------

	private ControlType controlType;
	private Set<Pathway> pathwayController;
	private Set<PhysicalEntity> peController;
	private Set<Process> controlled;

// --------------------------- CONSTRUCTORS ---------------------------

	public ControlImpl()
	{
		pathwayController = BPCollections.I.createSafeSet();
		peController = BPCollections.I.createSafeSet();
		controlled = BPCollections.I.createSafeSet();
	}

// ------------------------ INTERFACE METHODS ------------------------

	public Class<? extends Control> getModelInterface()
	{
		return Control.class;
	}

// --------------------- Interface Control ---------------------

// --------------------- ACCESORS and MUTATORS---------------------

	public ControlType getControlType()
	{
		return controlType;
	}

	public void setControlType(ControlType ControlType)
	{
		this.controlType = ControlType;
	}

	public Set<Process> getControlled()
	{
		return this.controlled;
	}

	public void addControlled(Process controlled)
	{
		if (controlled != null) {
			if (!checkControlled(controlled)) {
				throw new IllegalBioPAXArgumentException("Illegal argument. Attempting to set "
								+ controlled.getUri() + " to " + this.getUri());
			} else { //TODO: in paxtools v6, disallow multiple values (OWL functional property)...
				this.controlled.add(controlled);
				controlled.getControlledOf().add(this);
				super.addParticipant(controlled);
			}
		}
	}

	public void removeControlled(Process controlled)
	{
		if(controlled != null) {
			super.removeParticipant(controlled);
			controlled.getControlledOf().remove(this);
			this.controlled.remove(controlled);
		}
	}

	public Set<Controller> getController()
	{
		Set<Controller> controller = new HashSet<Controller>(this.getPeController());
		controller.addAll(getPathwayController());
		return Collections.unmodifiableSet(controller);
	}

	public void addController(Controller controller)
	{
		if (controller != null) {
			if (controller instanceof Pathway) {
				pathwayController.add((Pathway) controller);
			} else {
				peController.add((PhysicalEntity) controller);
			}
			controller.getControllerOf().add(this);
			super.addParticipant(controller);
		}
	}

	public void removeController(Controller controller)
	{
		if (controller != null) {
			super.removeParticipant(controller);
			controller.getControllerOf().remove(this);
			if (controller instanceof Pathway) {
				pathwayController.remove(controller);
			} else if(controller instanceof PhysicalEntity) {
				peController.remove(controller);
			} else { //hardly ever possible, anyway -
				throw new IllegalBioPAXArgumentException("removeController: argument " + controller.getUri()
					+ " is neither Pathway nor PE - " + controller.getModelInterface().getSimpleName());
			}
		}
	}


	// -------------------------- OTHER METHODS --------------------------
	protected boolean checkControlled(Process Controlled)
	{
		return true;
	}

	protected Set<Pathway> getPathwayController()
	{
		return pathwayController;
	}

	protected Set<PhysicalEntity> getPeController()
	{
		return peController;
	}

}
