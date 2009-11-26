package org.biopax.paxtools.impl.level2;

import org.biopax.paxtools.model.level2.*;

import java.util.HashSet;
import java.util.Set;

/**
 */
abstract class processImpl extends entityImpl implements process
{
// ------------------------------ FIELDS ------------------------------

	private Set<evidence> EVIDENCE;
	private Set<control> CONTROLLEDof;
	private Set<pathwayStep> STEP_INTERACTIONSOf;
	private Set<pathway> PATHWAY_COMPONENTSof;

// --------------------------- CONSTRUCTORS ---------------------------

	processImpl()
	{
		this.EVIDENCE = new HashSet<evidence>();
		this.CONTROLLEDof = new HashSet<control>();
		this.STEP_INTERACTIONSOf = new HashSet<pathwayStep>();
		this.PATHWAY_COMPONENTSof = new HashSet<pathway>();
	}

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface pathwayComponent ---------------------


	public Set<pathway> isPATHWAY_COMPONENTSof()
	{
		return PATHWAY_COMPONENTSof;
	}

// --------------------- Interface process ---------------------

// --------------------- ACCESORS and MUTATORS---------------------

	public Set<evidence> getEVIDENCE()
	{
		return EVIDENCE;
	}

	public void setEVIDENCE(Set<evidence> EVIDENCE)
	{
		this.EVIDENCE = EVIDENCE;
	}

	public void addEVIDENCE(evidence EVIDENCE)
	{
		this.EVIDENCE.add(EVIDENCE);
	}

	public void removeEVIDENCE(evidence EVIDENCE)
	{
		this.EVIDENCE.remove(EVIDENCE);
	}

	public Set<pathwayStep> isSTEP_INTERACTIONSOf()
	{
		return STEP_INTERACTIONSOf;
	}

	public Set<control> isCONTROLLEDOf()
	{
		return CONTROLLEDof;
	}

// -------------------------- OTHER METHODS --------------------------

	public void addCONTROLLEDof(control aControl)
	{
		this.CONTROLLEDof.add(aControl);
	}

	public void addSTEP_INTERACTIONSOf(pathwayStep aPathwayStep)
	{
		assert aPathwayStep.getSTEP_INTERACTIONS().contains(this);
		this.STEP_INTERACTIONSOf.add(aPathwayStep);
	}
}     
