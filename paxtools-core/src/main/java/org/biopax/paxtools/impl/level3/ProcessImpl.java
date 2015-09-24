package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.PathwayStep;
import org.biopax.paxtools.model.level3.Process;
import org.biopax.paxtools.util.BPCollections;

import java.util.Set;


public abstract class ProcessImpl extends EntityImpl implements Process
{
// ------------------------------ FIELDS ------------------------------

	private Set<Control> controlledOf;
	private Set<PathwayStep> stepProcessOf;
	private Set<Pathway> pathwayComponentOf;

// --------------------------- CONSTRUCTORS ---------------------------

	public ProcessImpl()
	{
		this.controlledOf = BPCollections.I.createSafeSet();
		this.stepProcessOf = BPCollections.I.createSafeSet();
		this.pathwayComponentOf = BPCollections.I.createSafeSet();
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface PathwayComponent ---------------------

	public Set<Pathway> getPathwayComponentOf()
	{
		return pathwayComponentOf;
	}

// --------------------- Interface process ---------------------

	public Set<PathwayStep> getStepProcessOf()
	{
		return stepProcessOf;
	}

	public Set<Control> getControlledOf()
	{
		return controlledOf;
	}

}
