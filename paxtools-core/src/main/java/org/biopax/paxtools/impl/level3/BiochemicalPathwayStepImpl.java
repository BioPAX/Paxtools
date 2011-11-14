package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.BiochemicalPathwayStep;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.StepDirection;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;

@Entity
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class BiochemicalPathwayStepImpl extends PathwayStepImpl implements BiochemicalPathwayStep
{
	Conversion stepConversion;
	StepDirection stepDirection;

	public BiochemicalPathwayStepImpl() {
	}
	
	//
	// utilityClass interface implementation
	//
	////////////////////////////////////////////////////////////////////////////
	@Transient
    public Class<? extends BiochemicalPathwayStep> getModelInterface()
    {
        return BiochemicalPathwayStep.class;
    }

	//
	// biochemicalPathwayStep interface implementation
	//
	////////////////////////////////////////////////////////////////////////////

	// Property STEP-CONVERSION
	@ManyToOne(targetEntity = ConversionImpl.class)//, cascade = {CascadeType.ALL})
    public Conversion getStepConversion()
	{
		return stepConversion;
	}

    public void setStepConversion(Conversion newSTEP_CONVERSION)
	{
		stepConversion = newSTEP_CONVERSION;
	}

    // Property STEP-DIRECTION

	@Enumerated(EnumType.STRING)
    public StepDirection getStepDirection()
	{
		return stepDirection;
	}

    public void setStepDirection(StepDirection newSTEP_DIRECTION)
	{
		stepDirection = newSTEP_DIRECTION;
	}
}
