/*
 * SequenceIntervalProxy.java
 *
 * 2007.04.06 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.sequenceInterval;
import org.biopax.paxtools.model.level2.sequenceSite;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Proxy for sequenceInterval
 */
@Entity(name="l2sequenceinterval")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class sequenceIntervalProxy extends sequenceLocationProxy implements sequenceInterval, Serializable {
	public sequenceIntervalProxy() {
	}

	@Transient
	public Class getModelInterface()
	{
		return sequenceInterval.class;
	}

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity=sequenceSiteProxy.class)
	@JoinColumn(name="sequence_interval_begin_x")
	public sequenceSite getSEQUENCE_INTERVAL_BEGIN() {
		return ((sequenceInterval)object).getSEQUENCE_INTERVAL_BEGIN();
	}

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity=sequenceSiteProxy.class)
	@JoinColumn(name="sequence_interval_end_x")
	public sequenceSite getSEQUENCE_INTERVAL_END() {
		return ((sequenceInterval)object).getSEQUENCE_INTERVAL_END();
	}

	public void setSEQUENCE_INTERVAL_BEGIN(sequenceSite SEQUENCE_INTERVAL_BEGIN) {
		((sequenceInterval)object).setSEQUENCE_INTERVAL_BEGIN(SEQUENCE_INTERVAL_BEGIN);
	}

	public void setSEQUENCE_INTERVAL_END(sequenceSite SEQUENCE_INTERVAL_END) {
		((sequenceInterval)object).setSEQUENCE_INTERVAL_END(SEQUENCE_INTERVAL_END);
	}
}
