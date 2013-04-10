package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.util.ChildDataStringBridge;
import org.biopax.paxtools.util.OrganismFieldBridge;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate; 
import org.hibernate.search.annotations.Boost;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@javax.persistence.Entity
@Proxy(proxyClass= Pathway.class)
@Indexed
@Boost(1.7f)
@DynamicUpdate @DynamicInsert
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
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

	@Field(name=FIELD_KEYWORD, store=Store.YES, analyze=Analyze.YES, bridge= @FieldBridge(impl = ChildDataStringBridge.class))
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
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

	// TODO not sure whether "data" index should be generated following pathwayOrder property (may have processes from other pathways)
	//	@Field(name=FIELD_KEYWORD, store=Store.YES, analyze=Analyze.YES, bridge= @FieldBridge(impl = ChildDataStringBridge.class))
	@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@OneToMany(targetEntity = PathwayStepImpl.class, mappedBy = "pathwayOrderOf")
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


    @Field(name=FIELD_ORGANISM, store=Store.YES, analyze=Analyze.NO)
    @FieldBridge(impl=OrganismFieldBridge.class)
	@ManyToOne(targetEntity = BioSourceImpl.class)
	public BioSource getOrganism()
	{
		return organism;
	}

	public void setOrganism(BioSource organism)
	{
		this.organism = organism;
	}

    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
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
