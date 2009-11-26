/*
 * processProxy.java
 *
 * 2007.11.39 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.model.level3.Process;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import java.io.Serializable;
import java.util.Set;

/**
 * Proxy for process
 */
@Entity(name="l3process")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public abstract class ProcessProxy extends EntityProxy implements Process, Serializable {
	protected ProcessProxy() {
	}

// Observable
/*
	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity= EvidenceProxy.class)
	@JoinTable(name="l3process_evidence")
	public Set<Evidence> getEvidence() {
		return ((Process)object).getEvidence();
	}

	public void addEVIDENCE(Evidence EVIDENCE) {
		((Process)object).addEVIDENCE(EVIDENCE);
	}

	public void removeEVIDENCE(Evidence EVIDENCE) {
		((Process)object).removeEVIDENCE(EVIDENCE);
	}

	public void setEvidence(Set<Evidence> EVIDENCE) {
		((Process)object).setEvidence(EVIDENCE);
	}
*/

// Process

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
