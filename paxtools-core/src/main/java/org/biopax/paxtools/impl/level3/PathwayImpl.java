package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;


@javax.persistence.Entity
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class PathwayImpl extends ProcessImpl implements Pathway
{
// ------------------------------ FIELDS ------------------------------

	private Set<Process> pathwayComponent;
	private Set<PathwayStep> pathwayOrder;
	private BioSource organism;
	private Set<Control> controllerOf;

// --------------------------- CONSTRUCTORS ---------------------------

	public PathwayImpl()
	{
		this.pathwayComponent = new HashSet<Process>();
		this.pathwayOrder = new HashSet<PathwayStep>();
		this.controllerOf = new HashSet<Control>();

	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------


	@Transient
	public Class<? extends Pathway> getModelInterface()
	{
		return Pathway.class;
	}

// --------------------- Interface Pathway ---------------------

// --------------------- ACCESORS and MUTATORS---------------------


	@ManyToMany(targetEntity = ProcessImpl.class)
	@JoinTable(name="pathwayComponent") 		
	public Set<Process> getPathwayComponent()
	{
		return this.pathwayComponent;
	}

	protected void setPathwayComponent(Set<Process> pathwayComponent)
	{
		this.pathwayComponent = pathwayComponent;
	}

	public void addPathwayComponent(Process component)
	{
		if (component != null) {
			this.pathwayComponent.add(component);
			component.getPathwayComponentOf().add(this);
		}
	}

	public void removePathwayComponent(Process component)
	{
		if (component != null) {
			this.pathwayComponent.remove(component);
			component.getPathwayComponentOf().remove(this);
		}
	}

	@OneToMany(targetEntity = PathwayStepImpl.class, mappedBy = "pathwayOrderOf")//, cascade = {CascadeType.ALL})
	public Set<PathwayStep> getPathwayOrder()
	{
		return pathwayOrder;
	}

	protected void setPathwayOrder(Set<PathwayStep> pathwayOrder)
	{
		this.pathwayOrder = pathwayOrder;
	}

	public void addPathwayOrder(PathwayStep pathwayOrder)
	{
		if (pathwayOrder != null) {
			this.pathwayOrder.add(pathwayOrder);
			((PathwayStepImpl) pathwayOrder).setPathwayOrderOf(this);
		}
	}

	public void removePathwayOrder(PathwayStep pathwayOrder)
	{
		if (pathwayOrder != null) {
			this.pathwayOrder.remove(pathwayOrder);
			((PathwayStepImpl) pathwayOrder).setPathwayOrderOf(null);
		}
	}


	@ManyToOne(targetEntity = BioSourceImpl.class)//, cascade = {CascadeType.ALL})
	public BioSource getOrganism()
	{
		return organism;
	}

	public void setOrganism(BioSource organism)
	{
		this.organism = organism;
	}

	@ManyToMany(targetEntity = ControlImpl.class, mappedBy = "pathwayController")
	public Set<Control> getControllerOf()
	{
		return controllerOf;
	}

	protected void setControllerOf(Set<Control> controllerOf)
	{
		this.controllerOf = controllerOf;
	}
}
