package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.PathwayStep;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.util.OrganismFieldBridge;
import org.biopax.paxtools.util.ParentPathwayFieldBridge;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate; 
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Fields;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Store;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Set;

@Entity
@Proxy(proxyClass= Process.class)
@DynamicUpdate @DynamicInsert
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public abstract class ProcessImpl extends EntityImpl implements Process
{
// ------------------------------ FIELDS ------------------------------


	private Set<Control> controlledOf;
	private Set<PathwayStep> stepProcessOf;
	private Set<Pathway> pathwayComponentOf;

// --------------------------- CONSTRUCTORS ---------------------------

	public ProcessImpl()
	{
		this.controlledOf = new HashSet<Control>();
		this.stepProcessOf = new HashSet<PathwayStep>();
		this.pathwayComponentOf = new HashSet<Pathway>();
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface PathwayComponent ---------------------


	@Fields({
		@Field(name=FIELD_PATHWAY, store=Store.YES, analyze=Analyze.YES, bridge=@FieldBridge(impl=ParentPathwayFieldBridge.class)),
		@Field(name=FIELD_ORGANISM, store=Store.YES, analyze=Analyze.NO, bridge=@FieldBridge(impl=OrganismFieldBridge.class))
	})
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = PathwayImpl.class, mappedBy = "pathwayComponent")
	public Set<Pathway> getPathwayComponentOf()
	{
		return pathwayComponentOf;
	}

// --------------------- Interface process ---------------------

    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = PathwayStepImpl.class, mappedBy = "stepProcess")
    @Field(name=FIELD_PATHWAY, store=Store.YES, analyze=Analyze.YES, bridge=@FieldBridge(impl=ParentPathwayFieldBridge.class))
	public Set<PathwayStep> getStepProcessOf()
	{
		return stepProcessOf;
	}

    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = ControlImpl.class, mappedBy = "controlled")
    @Field(name=FIELD_PATHWAY, store=Store.YES, analyze=Analyze.YES, bridge=@FieldBridge(impl=ParentPathwayFieldBridge.class))
	public Set<Control> getControlledOf()
	{
		return controlledOf;
	}

	protected void setControlledOf(Set<Control> controlledOf)
	{
		this.controlledOf = controlledOf;
	}

	protected void setStepProcessOf(Set<PathwayStep> stepProcessOf)
	{
		this.stepProcessOf = stepProcessOf;
	}

	protected void setPathwayComponentOf(Set<Pathway> pathwayComponentOf)
	{
		this.pathwayComponentOf = pathwayComponentOf;
	}
}
