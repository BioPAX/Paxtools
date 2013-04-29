package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.BiochemicalPathwayStep;
import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.Evidence;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.PathwayStep;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.util.BiopaxSafeSet;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate; 
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Proxy(proxyClass=PathwayStep.class)
@Indexed
@DynamicUpdate @DynamicInsert
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
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
		this.nextStep = new BiopaxSafeSet<PathwayStep>();
		this.nextStepOf = new BiopaxSafeSet<PathwayStep>();
		this.stepProcess = new BiopaxSafeSet<Process>();
		this.evidence = new BiopaxSafeSet<Evidence>();
	}

	@Transient
	public Class<? extends PathwayStep> getModelInterface()
	{
		return PathwayStep.class;
	}

    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = PathwayStepImpl.class)
	@JoinTable(name="nextStep")
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

	protected void setNextStep(Set<PathwayStep> nextStep)
	{
		this.nextStep = nextStep;
	}

    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = PathwayStepImpl.class, mappedBy = "nextStep")
	public Set<PathwayStep> getNextStepOf()
	{
		return nextStepOf;
	}

	protected void setNextStepOf(Set<PathwayStep> nextStepOf)
	{
		this.nextStepOf = nextStepOf;
	}

    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = ProcessImpl.class)
	@JoinTable(name="stepProcess")
	public Set<Process> getStepProcess()
	{
		return stepProcess;
	}

	public void addStepProcess(Process processStep)
	{
		if (processStep != null) {
			
			if(this instanceof BiochemicalPathwayStep 
				&& !(processStep instanceof Control)) {
				throw new IllegalArgumentException(
					"Range violation: BiochemicalPathwayStep.stepProcess "
						+ "can add only Control interactions.");	
			}
			
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

	public void setStepProcess(Set<Process> stepProcess)
	{
		this.stepProcess = stepProcess;
	}

    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
	@ManyToMany(targetEntity = EvidenceImpl.class)
	@JoinTable(name="evidence") 	
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

	public void setEvidence(Set<Evidence> evidence)
	{
		this.evidence = evidence;
	}

	@ManyToOne(targetEntity = PathwayImpl.class)
	public Pathway getPathwayOrderOf()
	{
		return this.pathwayOrderOf;
	}


	protected void setPathwayOrderOf(Pathway pathwayOrderOf)
	{
		this.pathwayOrderOf = pathwayOrderOf;
	}
}
