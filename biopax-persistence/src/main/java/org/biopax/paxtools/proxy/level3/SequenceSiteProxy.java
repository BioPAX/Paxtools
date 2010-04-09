/*
 * SequenceSiteProxy.java
 *
 * 2007.12.04 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;

/**
 * Proxy for sequenceSite
 */
@Entity(name="l3sequencesite")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class SequenceSiteProxy extends SequenceLocationProxy 
	implements SequenceSite
{
	// Property POSITION-STATUS

	@Basic @Enumerated @Column(name="position_status_x")
	public PositionStatusType getPositionStatus() {
		return ((SequenceSite)object).getPositionStatus();
	}

	public void setPositionStatus(PositionStatusType POSITION_STATUS) {
		((SequenceSite)object).setPositionStatus(POSITION_STATUS);
	}

    // Property SEQUENCE-POSITION

	@Basic @Column(name="sequence_position_x")
	public int getSequencePosition() {
		return ((SequenceSite)object).getSequencePosition();
	}

	public void setSequencePosition(int SEQUENCE_POSITION) {
		((SequenceSite)object).setSequencePosition(SEQUENCE_POSITION);
	}
	
	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return SequenceSite.class;
	}
}
