package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.Evidence;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.PathwayStep;
import org.biopax.paxtools.model.level3.Process;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import java.util.HashSet;
import java.util.Set;

@Entity
@Indexed(index=BioPAXElementImpl.SEARCH_INDEX_FOR_UTILILTY_CLASS)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
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
		this.nextStep = new HashSet<PathwayStep>();
		this.nextStepOf = new HashSet<PathwayStep>();
		this.stepProcess = new HashSet<Process>();
		this.evidence = new HashSet<Evidence>();
	}

	@Transient
	public Class<? extends PathwayStep> getModelInterface()
	{
		return PathwayStep.class;
	}


	@ManyToMany(targetEntity = PathwayStepImpl.class, cascade={CascadeType.ALL})
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

	protected void setNextStep(Set<PathwayStep> nextStep)
	{
		this.nextStep = nextStep;
	}

	@ManyToMany(targetEntity = PathwayStepImpl.class, mappedBy = "nextStep")
	public Set<PathwayStep> getNextStepOf()
	{
		return nextStepOf;
	}

	protected void setNextStepOf(Set<PathwayStep> nextStepOf)
	{
		this.nextStepOf = nextStepOf;
	}


	@ManyToMany(targetEntity = ProcessImpl.class, cascade={CascadeType.ALL})
	public Set<Process> getStepProcess()
	{
		return stepProcess;
	}

	public void addStepProcess(Process processStep)
	{
		this.stepProcess.add(processStep);
		processStep.getStepProcessOf().add(this);
	}

	public void removeStepProcess(Process processStep)
	{
		processStep.getStepProcessOf().remove(this);
		this.stepProcess.remove(processStep);
	}

	public void setStepProcess(Set<Process> stepProcess)
	{
		this.stepProcess = stepProcess;
	}

	@ManyToMany(targetEntity = EvidenceImpl.class, cascade={CascadeType.ALL})
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

	@ManyToOne(targetEntity = PathwayImpl.class, cascade = {CascadeType.ALL})
	public Pathway getPathwayOrderOf()
	{
		return this.pathwayOrderOf;
	}


	protected void setPathwayOrderOf(Pathway pathwayOrderOf)
	{
		this.pathwayOrderOf = pathwayOrderOf;
	}
}
