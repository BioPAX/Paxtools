package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.BiochemicalPathwayStep;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.StepDirection;
import org.biopax.paxtools.model.BioPAXElement;

class BiochemicalPathwayStepImpl extends PathwayStepImpl implements BiochemicalPathwayStep
{
	Conversion stepConversion;
	StepDirection stepDirection;

	//
	// utilityClass interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

    public Class<? extends BiochemicalPathwayStep> getModelInterface()
    {
        return BiochemicalPathwayStep.class;
    }

	//
	// biochemicalPathwayStep interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	// Property STEP-CONVERSION

    public Conversion getStepConversion()
	{
		return stepConversion;
	}

    public void setStepConversion(Conversion newSTEP_CONVERSION)
	{
		stepConversion = newSTEP_CONVERSION;
	}

    // Property STEP-DIRECTION

    public StepDirection getStepDirection()
	{
		return stepDirection;
	}

    public void setStepDirection(StepDirection newSTEP_DIRECTION)
	{
		stepDirection = newSTEP_DIRECTION;
	}
}
