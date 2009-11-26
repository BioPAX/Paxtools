/*
 * SequenceIntervalProxy.java
 *
 * 2007.12.04 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;
import java.io.Serializable;

/**
 * Proxy for sequenceInterval
 */
@Entity(name="l3sequenceinterval")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class SequenceIntervalProxy extends SequenceLocationProxy implements
	SequenceInterval, Serializable {
	public SequenceIntervalProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return SequenceInterval.class;
	}

    // Property SEQUENCE-INTERVAL-BEGIN

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity= SequenceSiteProxy.class)
	@JoinColumn(name="sequence_interval_begin_x")
	public SequenceSite getSequenceIntervalBegin() {
		return ((SequenceInterval)object).getSequenceIntervalBegin();
	}

	public void setSequenceIntervalBegin(SequenceSite SEQUENCE_INTERVAL_BEGIN) {
		((SequenceInterval)object).setSequenceIntervalBegin(SEQUENCE_INTERVAL_BEGIN);
	}

    // Property SEQUENCE-INTERVAL-END

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity= SequenceSiteProxy.class)
	@JoinColumn(name="sequence_interval_end_x")
	public SequenceSite getSequenceIntervalEnd() {
		return ((SequenceInterval)object).getSequenceIntervalEnd();
	}

	public void setSequenceIntervalEnd(SequenceSite SEQUENCE_INTERVAL_END) {
		((SequenceInterval)object).setSequenceIntervalEnd(SEQUENCE_INTERVAL_END);
	}
}
