package org.biopax.paxtools.model.level3;


import java.util.Set;

/**
 * This represents a set of pathway events.
 */
public interface PathwayStep extends UtilityClass,Observable
{

    // Property NEXT-STEP

    Set<PathwayStep> getNextStep();

    void addNextStep(PathwayStep newNEXT_STEP);

    void removeNextStep(PathwayStep oldNEXT_STEP);


	// Inverse of Property NEXT-STEP

	Set<PathwayStep> getNextStepOf();

    // Property STEP-INTERACTION

    Set<Process> getStepProcess();

    void addStepProcess(Process newSTEP_INTERACTIONS);

    void removeStepProcess(Process oldSTEP_INTERACTIONS);
    
    
    Pathway getPathwayOrderOf();

}
