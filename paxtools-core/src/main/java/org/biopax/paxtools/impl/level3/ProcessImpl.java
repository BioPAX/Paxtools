package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.PathwayStep;
import org.biopax.paxtools.model.level3.Process;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Set;

@Entity
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
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


	@ManyToMany(targetEntity = PathwayImpl.class, mappedBy = "pathwayComponent")
	public Set<Pathway> getPathwayComponentOf()
	{
		return pathwayComponentOf;
	}

// --------------------- Interface process ---------------------

	@ManyToMany(targetEntity = PathwayStepImpl.class, mappedBy = "stepProcess")
	public Set<PathwayStep> getStepProcessOf()
	{
		return stepProcessOf;
	}

	@ManyToMany(targetEntity = ControlImpl.class, mappedBy = "controlled")
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
