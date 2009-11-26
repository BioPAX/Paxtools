package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.Control;
import org.biopax.paxtools.model.level3.Pathway;
import org.biopax.paxtools.model.level3.PathwayStep;
import org.biopax.paxtools.model.level3.Process;

import java.util.HashSet;
import java.util.Set;

/**
 */
abstract class ProcessImpl extends EntityImpl implements Process
{
// ------------------------------ FIELDS ------------------------------

	private Set<Control> controlledOf;
	private Set<PathwayStep> stepInteractionsOf;
	private Set<Pathway> pathwayComponentsOf;

// --------------------------- CONSTRUCTORS ---------------------------

	ProcessImpl()
	{
		this.controlledOf = new HashSet<Control>();
		this.stepInteractionsOf = new HashSet<PathwayStep>();
		this.pathwayComponentsOf = new HashSet<Pathway>();
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface PathwayComponent ---------------------


	public Set<Pathway> getPathwayComponentsOf()
	{
		return pathwayComponentsOf;
	}

// --------------------- Interface process ---------------------

//TODO evidence
	public Set<PathwayStep> getStepInteractionsOf()
	{
		return stepInteractionsOf;
	}

	public Set<Control> getControlledOf()
	{
		return controlledOf;
	}

// -------------------------- OTHER METHODS --------------------------

	public void addControlledOf(Control control)
	{
		this.controlledOf.add(control);
	}

	public void addStepInteractionsOf(PathwayStep aPathwayStep)
	{
		assert aPathwayStep.getStepProcess().contains(this);
		this.stepInteractionsOf.add(aPathwayStep);
	}
}
