package org.biopax.paxtools.model.level3;

/**
 * This class represents  an ordered step in a biochemical pathway. A
 * biochemical reaction can be reversible by itself, but can be physiologically
 * directed in the context of a pathway, for instance due to flux of reactants
 * and products. Only one conversion interaction can be ordered at a time, but
 * multiple catalysis or modulation instances can be part of one step.
 */
public interface BiochemicalPathwayStep extends PathwayStep
{

    // Property STEP-CONVERSION

	Conversion getStepConversion();

	void setStepConversion(Conversion newSTEP_CONVERSION);

	// Property STEP-DIRECTION

	StepDirection getStepDirection();

	void setStepDirection(StepDirection newSTEP_DIRECTION);
}
