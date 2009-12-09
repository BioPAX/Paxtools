package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.ControlType;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;

import java.util.HashSet;
import java.util.Set;

/**
 */
class ControlImpl extends RestrictedInteractionAdapter
        implements Control
{
// ------------------------------ FIELDS ------------------------------

    private ControlType controlType;
    private Set<PhysicalEntity> controller;
    private Set<Process> controlled;

// --------------------------- CONSTRUCTORS ---------------------------

    public ControlImpl()
    {
        this.controller = new HashSet<PhysicalEntity>();
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
        addSubParticipant(controlled);
    }

    public void removeControlled(Process controlled)
    {
        removeSubParticipant(controlled);
        this.controlled.remove(controlled);
    }

    public Set<PhysicalEntity> getController()
    {
        return controller;
    }

    public void setController(Set<PhysicalEntity> controller)
    {
        //this.controller = controller;
    	for(PhysicalEntity pe : controller) {
    		addController(pe);
    	}
    }

    public void addController(PhysicalEntity controller)
    {
        this.controller.add(controller);
        addSubParticipant(controller);
    }

    public void removeController(PhysicalEntity controller)
    {
        removeSubParticipant(controller);
        this.controller.remove(controller);
    }

    // -------------------------- OTHER METHODS --------------------------
    protected boolean checkControlled(Process Controlled)
    {
        return true;
    }
}
