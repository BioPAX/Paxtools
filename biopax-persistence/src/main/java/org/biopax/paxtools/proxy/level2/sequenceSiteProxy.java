/*
 * SequenceSiteProxy.java
 *
 * 2007.04.06 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.PositionStatusType;
import org.biopax.paxtools.model.level2.sequenceSite;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Proxy for sequenceSite
 */
@Entity(name="l2sequencesite")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class sequenceSiteProxy extends sequenceLocationProxy implements sequenceSite, Serializable {
	public sequenceSiteProxy() {
	}

	@Transient
	public Class getModelInterface()
	{
		return sequenceSite.class;
	}

	@Basic @Enumerated @Column(name="position_status_x")
	public PositionStatusType getPOSITION_STATUS() {
		return ((sequenceSite)object).getPOSITION_STATUS();
	}

	@Basic @Column(name="sequence_position_x")
	public int getSEQUENCE_POSITION() {
		return ((sequenceSite)object).getSEQUENCE_POSITION();
	}

	public void setPOSITION_STATUS(PositionStatusType POSITION_STATUS) {
		((sequenceSite)object).setPOSITION_STATUS(POSITION_STATUS);
	}

	public void setSEQUENCE_POSITION(int SEQUENCE_POSITION) {
		((sequenceSite)object).setSEQUENCE_POSITION(SEQUENCE_POSITION);
	}
}
