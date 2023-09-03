package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.util.BPCollections;
import org.biopax.paxtools.util.SetEquivalenceChecker;

import java.util.Set;


public class PathwayStepImpl extends L3ElementImpl implements PathwayStep
{
	private Set<Process> stepProcess;
	private Set<PathwayStep> nextStep;
	private Set<PathwayStep> nextStepOf;
	private Pathway pathwayOrderOf;
	private Set<Evidence> evidence;

	/**
	 * Constructor.
	 */
	public PathwayStepImpl()
	{
		this.nextStep = BPCollections.I.createSafeSet();
		this.nextStepOf = BPCollections.I.createSafeSet();
		this.stepProcess = BPCollections.I.createSafeSet();
		this.evidence = BPCollections.I.createSafeSet();
	}

	public Class<? extends PathwayStep> getModelInterface()
	{
		return PathwayStep.class;
	}

	public Set<PathwayStep> getNextStep()
	{
		return nextStep;
	}

	public void addNextStep(PathwayStep nextStep)
	{
		if (nextStep != null) {
			this.nextStep.add(nextStep);
			nextStep.getNextStepOf().add(this);
		}
	}

	public void removeNextStep(PathwayStep nextStep)
	{
		if (nextStep != null) {
			nextStep.getNextStepOf().remove(this);
			this.nextStep.remove(nextStep);
		}
	}

	public Set<PathwayStep> getNextStepOf()
	{
		return nextStepOf;
	}

	public Set<Process> getStepProcess()
	{
		return stepProcess;
	}

	public void addStepProcess(Process processStep)
	{
		if (processStep != null) {
			
			this.stepProcess.add(processStep);
			processStep.getStepProcessOf().add(this);
		}
	}

	public void removeStepProcess(Process processStep)
	{
		if (processStep != null) {
			processStep.getStepProcessOf().remove(this);
			this.stepProcess.remove(processStep);
		}
	}

	public Set<Evidence> getEvidence()
	{
		return evidence;
	}

	public void addEvidence(Evidence evidence)
	{
		if (evidence != null)
			this.evidence.add(evidence);
	}

	public void removeEvidence(Evidence evidence)
	{
		if (evidence != null)
			this.evidence.remove(evidence);
	}

	public Pathway getPathwayOrderOf()
	{
		return this.pathwayOrderOf;
	}

	protected void setPathwayOrderOf(Pathway pathwayOrderOf)
	{
		this.pathwayOrderOf = pathwayOrderOf;
	}

	@Override
	protected boolean semanticallyEquivalent(BioPAXElement element) {
		boolean equivalence = false;
		if (element instanceof PathwayStep) {
			PathwayStep other = (PathwayStep) element;
			equivalence = SetEquivalenceChecker.hasEquivalentIntersection(evidence, other.getEvidence())
							&& SetEquivalenceChecker.isEquivalent(this.getStepProcess(), other.getStepProcess())
							&& SetEquivalenceChecker.isEquivalent(this.getNextStep(), other.getNextStep());
		}
		return equivalence;
	}
}
