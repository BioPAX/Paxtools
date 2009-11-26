/*
 * PathwayStepProxy.java
 *
 * 2007.11.29 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;
import java.io.Serializable;
import java.util.Set;

/**
 * Proxy for pathwayStep
 */
@Entity(name="l3pathwaystep")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class PathwayStepProxy extends UtilityClassProxy implements PathwayStep, Serializable {
	public PathwayStepProxy() {
	}

	@Transient
	public Class getModelInterface()
	{
		return PathwayStep.class;
	}

// utilityClass

// Observable

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity= EvidenceProxy.class)
	@JoinTable(name="l3pathwaystep_evidence")
	public Set<Evidence> getEvidence() {
		return ((PathwayStep)object).getEvidence();
	}

	public void addEvidence(Evidence newEvidence) {
		((PathwayStep)object).addEvidence(newEvidence);
	}

	public void removeEvidence(Evidence oldEvidence) {
		((PathwayStep)object).removeEvidence(oldEvidence);
	}

	public void setEvidence(Set<Evidence> newEvidence) {
		((PathwayStep)object).setEvidence(newEvidence);
	}

// pathwayStep

    // Property NEXT-STEP

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity= PathwayStepProxy.class)
	@JoinTable(name="l3pathwaystep_next_step")
	public Set<PathwayStep> getNextStep() {
		return ((PathwayStep)object).getNextStep();
	}

	public void addNextStep(PathwayStep NEXT_STEP) {
		((PathwayStep)object).addNextStep(NEXT_STEP);
	}

	public void removeNextStep(PathwayStep NEXT_STEP) {
		((PathwayStep)object).removeNextStep(NEXT_STEP);
	}

	public void setNextStep(Set<PathwayStep> NEXT_STEP) {
		((PathwayStep)object).setNextStep(NEXT_STEP);
	}

	// Inverse of Property NEXT-STEP

	@Transient
	public Set<PathwayStep> getNextStepOf() {
		return ((PathwayStep)object).getNextStepOf();
	}

    // Property STEP-INTERACTION

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity=ProcessProxy.class)
	@JoinTable(name="l3pathwaystep_step_process")
	public Set<Process> getStepProcess() {
		return ((PathwayStep)object).getStepProcess();
	}

	public void addStepProcess(Process newSTEP_INTERACTIONS) {
		((PathwayStep)object).addStepProcess(newSTEP_INTERACTIONS);
	}

	public void removeStepProcess(Process oldSTEP_INTERACTIONS) {
		((PathwayStep)object).removeStepProcess(oldSTEP_INTERACTIONS);
	}

	public void setStepProcess(Set<Process> newSTEP_INTERACTIONS) {
		((PathwayStep)object).setStepProcess(newSTEP_INTERACTIONS);
	}

	@Transient
	public Set<Pathway> getPathwayOrdersOf() {
		return ((PathwayStep)object).getPathwayOrdersOf();
	}
}
