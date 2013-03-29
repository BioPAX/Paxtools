package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.util.IllegalBioPAXArgumentException;
import org.biopax.paxtools.util.ParentPathwayFieldBridge;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Proxy;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.hibernate.search.annotations.FieldBridge;

import javax.persistence.Entity;
import javax.persistence.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Proxy(proxyClass= Control.class)
@Indexed
@DynamicUpdate @DynamicInsert
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
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
		pathwayController = new HashSet<Pathway>();
		peController = new HashSet<PhysicalEntity>();
		controlled = new HashSet<Process>();
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

    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = ProcessImpl.class)
	@JoinTable(name="controlled")
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
		if (controlled != null) {
			if (!checkControlled(controlled)) {
				throw new IllegalBioPAXArgumentException(
						"Illegal argument. Attempting to set "
								+ controlled.getRDFId() + " to "
								+ this.getRDFId());

			}
			if (controlled != null) {
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

	@Transient
	public Set<Controller> getController()
	{
		Set<Controller> controller = new HashSet<Controller>(getPeController());
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
			} else {
				peController.remove(controller);
			}
		}
	}


	// -------------------------- OTHER METHODS --------------------------
	protected boolean checkControlled(Process Controlled)
	{
		return true;
	}

	
	@Field(name=FIELD_PATHWAY, store=Store.YES, analyze=Analyze.YES, bridge=@FieldBridge(impl=ParentPathwayFieldBridge.class))
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = PathwayImpl.class)//, cascade={CascadeType.ALL})
	@JoinTable(name="pathwayController")
	protected Set<Pathway> getPathwayController()
	{
		return pathwayController;
	}

	protected void setPathwayController(Set<Pathway> pathwayController)
	{
		this.pathwayController = pathwayController;
	}

    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = PhysicalEntityImpl.class)
	@JoinTable(name="peController")
	protected Set<PhysicalEntity> getPeController()
	{
		return peController;
	}

	protected void setPeController(Set<PhysicalEntity> peController)
	{
		this.peController = peController;
	}

}
