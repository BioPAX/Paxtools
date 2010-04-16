package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.util.HashSet;
import java.util.Set;

/**
 */
class ControlImpl extends InteractionImpl
        implements Control
{
// ------------------------------ FIELDS ------------------------------

    private ControlType controlType;
    private Set<Controller> controller;
    private Set<Process> controlled;

// --------------------------- CONSTRUCTORS ---------------------------

    public ControlImpl()
    {
        this.controller = new HashSet<Controller>();
        this.controlled = new HashSet<Process>();

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

    public void setControlled(Set<Process> controlled)
    {
    	//this.controlled = controlled;
    	for(Process pe : controlled) {
    		addControlled(pe);
    	}
    }

    public void addControlled(Process controlled)
    {
        if (!checkControlled(controlled))
        {
            throw new IllegalBioPAXArgumentException(
                    "Illegal argument. Attempting to set " +
                    controlled.getRDFId() +
                    " to " + this.getRDFId());
	        
        }
        this.controlled.add(controlled);
	    controlled.getControlledOf().add(this);
        super.addParticipant(controlled);
    }

    public void removeControlled(Process controlled)
    {
        super.removeParticipant(controlled);
        controlled.getControlledOf().remove(this);
	    this.controlled.remove(controlled);

    }

    public Set<Controller> getController()
    {
        return controller;
    }

    public void setController(Set<Controller> controllers)
    {
    	for(Controller controller : controllers) {
    		addController(controller);
    	}
    }

	public void addController(Controller controller)
    {
        this.controller.add(controller);
	    controller.getControllerOf().add(this);
        super.addParticipant(controller);
    }

    public void removeController(Controller controller)
    {
        super.removeParticipant(controller);
	    controller.getControllerOf().remove(this);
        this.controller.remove(controller);
    }


	// -------------------------- OTHER METHODS --------------------------
    protected boolean checkControlled(Process Controlled)
    {
        return true;
    }
}
