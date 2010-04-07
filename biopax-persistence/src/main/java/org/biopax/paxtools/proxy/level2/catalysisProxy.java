/*
 * CatalysisProxy.java
 *
 * 2007.04.06 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level2;

import org.biopax.paxtools.model.level2.*;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import java.util.Set;

/**
 * Proxy for catalysis
 */
@Entity(name="l2catalysis")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class catalysisProxy extends controlProxy implements catalysis {
	public catalysisProxy() {
	}

	@Transient
	public Class getModelInterface()
	{
		return catalysis.class;
	}

	public void addCOFACTOR(physicalEntityParticipant COFACTOR) {
		((catalysis)object).addCOFACTOR(COFACTOR);
	}

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity=physicalEntityParticipantProxy.class)
	@JoinTable(name="l2catalysis_cofactor")
	public Set<physicalEntityParticipant> getCOFACTOR() {
		return ((catalysis)object).getCOFACTOR();
	}

	@Basic @Enumerated @Column(name="direction_x")
	public Direction getDIRECTION() {
		return ((catalysis)object).getDIRECTION();
	}

	public void removeCOFACTOR(physicalEntityParticipant COFACTOR) {
		((catalysis)object).removeCOFACTOR(COFACTOR);
	}

	public void setCOFACTOR(Set<physicalEntityParticipant> COFACTOR) {
		((catalysis)object).setCOFACTOR(COFACTOR);
	}

	public void setDIRECTION(Direction DIRECTION) {
		((catalysis)object).setDIRECTION(DIRECTION);
	}
}
