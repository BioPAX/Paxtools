package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;

import java.util.HashSet;
import java.util.Set;


/**
 */
class PathwayImpl extends ProcessImpl implements Pathway
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




	public Class<? extends Pathway> getModelInterface()
	{
		return Pathway.class;
	}

// --------------------- Interface Pathway ---------------------

// --------------------- ACCESORS and MUTATORS---------------------


	public Set<Process> getPathwayComponent()
	{
		return this.pathwayComponent;
	}

	public void setPathwayComponent(Set<Process> pathwayComponent)
	{
        this.pathwayComponent = pathwayComponent;
    }

	public void addPathwayOrder(PathwayStep pathwayOrder)
	{
		this.pathwayOrder.add(pathwayOrder);
		pathwayOrder.getPathwayOrdersOf().add(this);

	}

	public void removePathwayOrder(PathwayStep pathwayOrder)
	{
		this.pathwayOrder.remove(pathwayOrder);
		pathwayOrder.getPathwayOrdersOf().remove(this);
		
	}

	public Set<PathwayStep> getPathwayOrder()
	{
		return pathwayOrder;
	}

	public void setPathwayOrder(Set<PathwayStep> pathwayOrder)
	{
		this.pathwayOrder = pathwayOrder;
	}

	public void addPathwayComponent(Process component)
	{
		this.pathwayComponent.add(component);
		component.getPathwayComponentsOf().add(this);
	}

	public void removePathwayComponent(Process component)
	{
		this.pathwayComponent.remove(component);
		component.getPathwayComponentsOf().remove(this);
	}


	public BioSource getOrganism()
	{
		return organism;
	}

	public void setOrganism(BioSource organism)
	{
		this.organism = organism;
	}


	@Override
	public Set<Control> getControllerOf()
	{
		return controllerOf;
	}
}
