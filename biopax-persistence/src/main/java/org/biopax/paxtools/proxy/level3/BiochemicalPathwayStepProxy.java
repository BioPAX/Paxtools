/*
 * BiochemicalPathwayStepProxy.java
 *
 * 2007.11.30 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.*;


/**
 * Proxy for biochemicalPathwayStep
 */
@Entity(name="l3biochemicalpathwaystep")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class BiochemicalPathwayStepProxy extends PathwayStepProxy<BiochemicalPathwayStep> implements
	BiochemicalPathwayStep {

	// Property STEP-CONVERSION

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity= ConversionProxy.class)
	@JoinColumn(name="step_conversion_x")
	public Conversion getStepConversion() {
		return object.getStepConversion();
	}

    public void setStepConversion(Conversion newSTEP_CONVERSION) {
		object.setStepConversion(newSTEP_CONVERSION);
    }

	// Property STEP-DIRECTION

	@Basic @Enumerated @Column(name="step_direction_x")
	public StepDirection getStepDirection() {
		return object.getStepDirection();
	}

	public void setStepDirection(StepDirection newSTEP_DIRECTION) {
		object.setStepDirection(newSTEP_DIRECTION);
	}
	
	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return BiochemicalPathwayStep.class;
	}
}

