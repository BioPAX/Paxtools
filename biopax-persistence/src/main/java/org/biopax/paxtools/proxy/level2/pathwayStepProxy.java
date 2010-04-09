/*
 * PathwayStepProxy.java
 *
 * 2007.04.06 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.pathwayStep;
import org.biopax.paxtools.model.level2.process;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import java.util.Set;

/**
 * Proxy for pathwayStep
 */
@Entity(name="l2pathwaystep")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class pathwayStepProxy extends pathwayComponentProxy implements pathwayStep {
	public pathwayStepProxy() {
	}

	@Transient
	public Class getModelInterface()
	{
		return pathwayStep.class;
	}



	public void addNEXT_STEP(pathwayStep NEXT_STEP) {
		((pathwayStep)object).addNEXT_STEP(NEXT_STEP);
	}

	public void addSTEP_INTERACTIONS(process processStep) {
		((pathwayStep)object).addSTEP_INTERACTIONS(processStep);
	}

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity=pathwayStepProxy.class)
	@JoinTable(name="l2pws_next_step")
	public Set<pathwayStep> getNEXT_STEP() {
		return ((pathwayStep)object).getNEXT_STEP();
	}

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity=processProxy.class)
	@JoinTable(name="l2pws_step_interactions")
	public Set<process> getSTEP_INTERACTIONS() {
		return ((pathwayStep)object).getSTEP_INTERACTIONS();
	}

	@Transient
	public Set<pathwayStep> isNEXT_STEPof() {
		return ((pathwayStep)object).isNEXT_STEPof();
	}

	public void removeNEXT_STEP(pathwayStep NEXT_STEP) {
		((pathwayStep)object).removeNEXT_STEP(NEXT_STEP);
	}

	public void removeSTEP_INTERACTIONS(process processStep) {
		((pathwayStep)object).removeSTEP_INTERACTIONS(processStep);
	}

	public void setNEXT_STEP(Set<pathwayStep> NEXT_STEP) {
		((pathwayStep)object).setNEXT_STEP(NEXT_STEP);
	}

	public void setSTEP_INTERACTIONS(Set<process> STEP_INTERACTIONS) {
		((pathwayStep)object).setSTEP_INTERACTIONS(STEP_INTERACTIONS);
	}
}
