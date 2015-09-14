package org.biopax.paxtools.model.level2;

import java.util.Set;

/**
 * This represents a set of pathway events.
 */
public interface pathwayStep extends utilityClass, pathwayComponent
{
// -------------------------- OTHER METHODS --------------------------

	void addNEXT_STEP(pathwayStep NEXT_STEP);

	void addSTEP_INTERACTIONS(process processStep);


	Set<pathwayStep> getNEXT_STEP();
// --------------------- ACCESORS and MUTATORS---------------------

	Set<process> getSTEP_INTERACTIONS();

	Set<pathwayStep> isNEXT_STEPof();

	void removeNEXT_STEP(pathwayStep NEXT_STEP);

	void removeSTEP_INTERACTIONS(process processStep);

	void setNEXT_STEP(Set<pathwayStep> NEXT_STEP);

	void setSTEP_INTERACTIONS(Set<process> STEP_INTERACTIONS);
}