package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level2.pathway;
import org.biopax.paxtools.model.level2.pathwayStep;
import org.biopax.paxtools.model.level2.process;

import java.util.HashSet;
import java.util.Set;

/**
 */
class pathwayStepImpl extends BioPAXLevel2ElementImpl implements pathwayStep
{
// ------------------------------ FIELDS ------------------------------

	private Set<process> STEP_INTERACTIONS;
	private Set<pathwayStep> NEXT_STEP;
	private Set<pathwayStep> NEXT_STEPof;
	private Set<pathway> PATHWAY_COMPONENTSof;

// --------------------------- CONSTRUCTORS ---------------------------

	public pathwayStepImpl()
	{
		this.NEXT_STEP = new HashSet<pathwayStep>();
		this.NEXT_STEPof = new HashSet<pathwayStep>();
		this.PATHWAY_COMPONENTSof = new HashSet<pathway>();
		this.STEP_INTERACTIONS = new HashSet<process>();
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BioPAXElement ---------------------



	public Class<? extends BioPAXElement> getModelInterface()
	{
		return pathwayStep.class;
	}

// --------------------- Interface pathwayComponent ---------------------


	public Set<pathway> isPATHWAY_COMPONENTSof()
	{
		return PATHWAY_COMPONENTSof;
	}

// --------------------- Interface pathwayStep ---------------------


	public Set<process> getSTEP_INTERACTIONS()
	{
		return STEP_INTERACTIONS;
	}

	public void setSTEP_INTERACTIONS(Set<process> STEP_INTERACTIONS)
	{
		if (this.STEP_INTERACTIONS != null)
		{
			for (process process : STEP_INTERACTIONS)
			{
				process.isSTEP_INTERACTIONSOf().remove(this);
			}
		}
		this.STEP_INTERACTIONS = STEP_INTERACTIONS;
		if (this.STEP_INTERACTIONS != null)
		{
			for (process process : STEP_INTERACTIONS)
			{
				process.isSTEP_INTERACTIONSOf().add(this);
			}
		}
	}

	public void addSTEP_INTERACTIONS(process processStep)
	{
		this.STEP_INTERACTIONS.add(processStep);
		processStep.isSTEP_INTERACTIONSOf().add(this);
	}

	public void removeSTEP_INTERACTIONS(process processStep)
	{
		processStep.isSTEP_INTERACTIONSOf().remove(this);
		this.STEP_INTERACTIONS.remove(processStep);
	}

// --------------------- ACCESORS and MUTATORS---------------`------

	public Set<pathwayStep> getNEXT_STEP()
	{
		return NEXT_STEP;
	}

	public void setNEXT_STEP(Set<pathwayStep> NEXT_STEP)
	{
		if (this.NEXT_STEP != null)
		{
			for (pathwayStep pathwayStep : this.NEXT_STEP)
			{
				pathwayStep.isNEXT_STEPof().remove(this);
			}
		}
		this.NEXT_STEP = NEXT_STEP;
		if (this.NEXT_STEP != null)
		{
			for (pathwayStep pathwayStep : NEXT_STEP)
			{
				pathwayStep.isNEXT_STEPof().add(this);
			}
		}
	}

	public void addNEXT_STEP(pathwayStep NEXT_STEP)
	{
		this.NEXT_STEP.add(NEXT_STEP);
		NEXT_STEP.isNEXT_STEPof().add(this);
	}

	public void removeNEXT_STEP(pathwayStep NEXT_STEP)
	{
		NEXT_STEP.isNEXT_STEPof().remove(this);
		this.NEXT_STEP.remove(NEXT_STEP);
	}

	public Set<pathwayStep> isNEXT_STEPof()
	{
		return NEXT_STEPof;
	}
}
