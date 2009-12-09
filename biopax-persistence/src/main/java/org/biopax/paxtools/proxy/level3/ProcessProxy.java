/*
 * processProxy.java
 *
 * 2007.11.39 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;

import javax.persistence.*;
import javax.persistence.Entity;
import java.util.Set;

/**
 * Proxy for process
 */
@Entity(name="l3process")
public abstract class ProcessProxy extends EntityProxy implements Process {
	protected ProcessProxy() {
	}

	@Transient
	public Set<Control> getControlledOf() {
		return ((Process)object).getControlledOf();
	}

	@Transient
	public Set<Pathway> getPathwayComponentsOf() {
		return ((Process)object).getPathwayComponentsOf();
	}

	@Transient
	public Set<PathwayStep> getStepInteractionsOf() {
		return ((Process)object).getStepInteractionsOf();
	}

}
