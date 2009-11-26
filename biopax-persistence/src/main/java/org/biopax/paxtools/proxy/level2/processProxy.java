/*
 * processProxy.java
 *
 * 2007.04.05 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * Proxy for process
 */
@Entity(name="l2process")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public abstract class processProxy extends entityProxy implements process, Serializable {

	protected processProxy() {
	}


	public void addEVIDENCE(evidence EVIDENCE) {
		((process)object).addEVIDENCE(EVIDENCE);
	}

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity=evidenceProxy.class)
	@JoinTable(name="l2process_evidence")
	public Set<evidence> getEVIDENCE() {
		return ((process)object).getEVIDENCE();
	}

	@Transient
	public Set<control> isCONTROLLEDOf() {
		return ((process)object).isCONTROLLEDOf();
	}

	@Transient
	public Set<pathwayStep> isSTEP_INTERACTIONSOf() {
		return ((process)object).isSTEP_INTERACTIONSOf();
	}

	public void removeEVIDENCE(evidence EVIDENCE) {
		((process)object).removeEVIDENCE(EVIDENCE);
	}

	public void setEVIDENCE(Set<evidence> EVIDENCE) {
		((process)object).setEVIDENCE(EVIDENCE);
	}

	@Transient
	public Set<pathway> isPATHWAY_COMPONENTSof() {
		return ((process)object).isPATHWAY_COMPONENTSof();
	}
}
