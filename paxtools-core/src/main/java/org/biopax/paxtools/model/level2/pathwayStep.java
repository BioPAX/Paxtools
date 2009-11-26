package org.biopax.paxtools.model.level2;

import java.util.Set;

/**
 * This represents a set of pathway events.
 */
public interface pathwayStep extends utilityClass, pathwayComponent
{
// -------------------------- OTHER METHODS --------------------------

	public void addNEXT_STEP(pathwayStep NEXT_STEP);

	public void addSTEP_INTERACTIONS(process processStep);


	public Set<pathwayStep> getNEXT_STEP();
// --------------------- ACCESORS and MUTATORS---------------------

	public Set<process> getSTEP_INTERACTIONS();

	public Set<pathwayStep> isNEXT_STEPof();

	public void removeNEXT_STEP(pathwayStep NEXT_STEP);

	public void removeSTEP_INTERACTIONS(process processStep);

	void setNEXT_STEP(Set<pathwayStep> NEXT_STEP);

	void setSTEP_INTERACTIONS(Set<process> STEP_INTERACTIONS);
}