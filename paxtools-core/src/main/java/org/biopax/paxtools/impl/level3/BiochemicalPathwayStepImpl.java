package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.BiochemicalPathwayStep;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.StepDirection;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Proxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;

@Entity
@Proxy(proxyClass= BiochemicalPathwayStep.class)
@Indexed
@DynamicUpdate @DynamicInsert
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
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

	// hidden property 'stepConversion' for persistence
	@ManyToOne(targetEntity = ConversionImpl.class)
    Conversion getStepConversionX()
	{
		return stepConversion;
	}
    void setStepConversionX(Conversion newSTEP_CONVERSION)
	{
		stepConversion = newSTEP_CONVERSION;
	}

	// Property stepConversion
    @Transient
    public Conversion getStepConversion()
	{
		return stepConversion;
	}

    public void setStepConversion(Conversion newSTEP_CONVERSION)
	{
		if (this.stepConversion != null)
		{
			this.stepConversion.getStepProcessOf().remove(this);
		}
		this.stepConversion = newSTEP_CONVERSION;
		if (this.stepConversion != null)
		{
			this.stepConversion.getStepProcessOf().add(this);
		}
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
