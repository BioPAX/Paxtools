package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Evidence;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.PathwayStep;
import org.biopax.paxtools.model.level3.Process;

import java.util.HashSet;
import java.util.Set;

/**
 */
class PathwayStepImpl extends L3ElementImpl implements PathwayStep
{

	private Set<Process> stepProcess;
	private Set<PathwayStep> nextStep;
	private Set<PathwayStep> nextStepOf;
	private Set<Pathway> pathwayOrdersOf;
	private Set<Evidence> evidence;

	/**
	 * Constructor.
	 */
	public PathwayStepImpl()
	{
		this.nextStep = new HashSet<PathwayStep>();
		this.nextStepOf = new HashSet<PathwayStep>();
		this.stepProcess = new HashSet<Process>();
		this.pathwayOrdersOf = new HashSet<Pathway>();
		this.evidence = new HashSet<Evidence>();
	}

	//
	// BioPAXElement interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	public Class<? extends PathwayStep> getModelInterface()
	{
		return PathwayStep.class;
	}


	//
	// PathwayComponent interface implementation (?)
	//
	////////////////////////////////////////////////////////////////////////////

	public Set<Pathway> isPathwayComponentOf()
	{
		return pathwayOrdersOf;
	}

	//
	// PathwayStep interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

    // Property NEXT-STEP

	public Set<PathwayStep> getNextStep()
	{
		return nextStep;
	}

	public void addNextStep(PathwayStep nextStep)
	{
		this.nextStep.add(nextStep);
		nextStep.getNextStepOf().add(this);
	}

	public void removeNextStep(PathwayStep nextStep)
	{
		nextStep.getNextStepOf().remove(this);
		this.nextStep.remove(nextStep);
	}

	public void setNextStep(Set<PathwayStep> nextStep)
	{
		if (this.nextStep != null)
		{
			for (PathwayStep PathwayStep : this.nextStep)
			{
				PathwayStep.getNextStepOf().remove(this);
			}
		}
		this.nextStep = nextStep;
		if (this.nextStep != null)
		{
			for (PathwayStep PathwayStep : nextStep)
			{
				PathwayStep.getNextStepOf().add(this);
			}
		}
	}

	public Set<PathwayStep> getNextStepOf()
	{
		return nextStepOf;
	}

    // Property STEP-INTERACTIONS (STEP-PROCESS)

	public Set<Process> getStepProcess()
	{
		return stepProcess;
	}

	public void addStepProcess(Process processStep)
	{
		this.stepProcess.add(processStep);
		processStep.getStepInteractionsOf().add(this);
	}

	public void removeStepProcess(Process processStep)
	{
		processStep.getStepInteractionsOf().remove(this);
		this.stepProcess.remove(processStep);
	}

	public void setStepProcess(Set<Process> stepProcess)
	{
		if (this.stepProcess != null)
		{
			for (Process process : stepProcess)
			{
				process.getStepInteractionsOf().remove(this);
			}
		}
		this.stepProcess = stepProcess;
		if (this.stepProcess != null)
		{
			for (Process process : stepProcess)
			{
				process.getStepInteractionsOf().add(this);
			}
		}
	}

	//
	// observable interface implementation
	//
	/////////////////////////////////////////////////////////////////////////////

	public Set<Evidence> getEvidence()
	{
		return evidence;
	}

	public void addEvidence(Evidence evidence)
	{
		this.evidence.add(evidence);
	}

	public void removeEvidence(Evidence evidence)
	{
		this.evidence.remove(evidence);
	}

	public void setEvidence(Set<Evidence> evidence)
    {
		this.evidence = evidence;
	}

	public Set<Pathway> getPathwayOrdersOf() {
		return this.pathwayOrdersOf;
	}
	
	public void addPathwayOrdersOf(Pathway aPathway)
	{
		assert aPathway.getPathwayOrder().contains(this);
		this.pathwayOrdersOf.add(aPathway);
	}
}
