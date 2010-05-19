package org.biopax.paxtools.impl.level3;

import org.apache.commons.collections15.set.CompositeSet;
import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;
import java.util.HashSet;
import java.util.Set;

@Entity
@Indexed(index=BioPAXElementImpl.SEARCH_INDEX_FOR_ENTITY)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class ControlImpl extends InteractionImpl
		implements Control
{
// ------------------------------ FIELDS ------------------------------

	private ControlType controlType;
	private Set<Pathway> pathwayController;


	private Set<PhysicalEntity> peController;
	private CompositeSet<Controller> controller;
	private Set<Process> controlled;

// --------------------------- CONSTRUCTORS ---------------------------

	public ControlImpl()
	{
		pathwayController = new HashSet<Pathway>();
		peController = new HashSet<PhysicalEntity>();
		controlled = new HashSet<Process>();
		controller = new CompositeSet<Controller>();
		controller.addComposited(peController);
		controller.addComposited(pathwayController);

	}

// ------------------------ INTERFACE METHODS ------------------------

	@Transient
	public Class<? extends Control> getModelInterface()
	{
		return Control.class;
	}

// --------------------- Interface Control ---------------------

// --------------------- ACCESORS and MUTATORS---------------------

	@Enumerated
	public ControlType getControlType()
	{
		return controlType;
	}

	public void setControlType(ControlType ControlType)
	{
		this.controlType = ControlType;
	}

	@ManyToMany(targetEntity = ProcessImpl.class, cascade={CascadeType.PERSIST})
	public Set<Process> getControlled()
	{
		return this.controlled;
	}

	protected void setControlled(Set<Process> controlled)
	{
		this.controlled = controlled;
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

	@Transient
	public Set<Controller> getController()
	{
		return controller;
	}

	public void addController(Controller controller)
	{
		if (controller instanceof Pathway)
		{
			pathwayController.add((Pathway) controller);
		}
		else
		{
			peController.add((PhysicalEntity) controller);
		}
		controller.getControllerOf().add(this);
		super.addParticipant(controller);
	}

	public void removeController(Controller controller)
	{
		super.removeParticipant(controller);
		controller.getControllerOf().remove(this);
		if (controller instanceof Pathway)
		{
			pathwayController.remove(controller);
		}
		else
		{
			peController.remove(controller);
		}
	}


	// -------------------------- OTHER METHODS --------------------------
	protected boolean checkControlled(Process Controlled)
	{
		return true;
	}

	@ManyToMany(targetEntity = PathwayImpl.class, cascade={CascadeType.PERSIST})
	Set<Pathway> getPathwayController()
	{
		return pathwayController;
	}


	void setPathwayController(Set<Pathway> pathwayController)
	{
		this.pathwayController = pathwayController;
	}

	@ManyToMany(targetEntity = PhysicalEntityImpl.class, cascade={CascadeType.PERSIST})
	Set<PhysicalEntity> getPeController()
	{
		return peController;
	}

	void setPeController(Set<PhysicalEntity> peController)
	{
		this.peController = peController;
	}

}
