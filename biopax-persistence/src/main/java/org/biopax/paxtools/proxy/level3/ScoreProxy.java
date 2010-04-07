/*
 * ScoreProxy.java
 *
 * 2007.12.03 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;

import java.util.Set;

/**
 * Proxy for Score
 */
@Entity(name="l3score")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class ScoreProxy extends Level3ElementProxy implements Score 
{
// XReferrable

	// Property Xref

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = XrefProxy.class)
	@JoinTable(name="l3score_xref")
	public Set<Xref> getXref() {
		return ((Score)object).getXref();
	}

	public void addXref(Xref XREF) {
		((Score)object).addXref(XREF);
	}

	public void removeXref(Xref XREF) {
		((Score)object).removeXref(XREF);
	}

	public void setXref(Set<Xref> XREF) {
		((Score)object).setXref(XREF);
	}

	@Basic @Column(name="value_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
    public String getValue() {
		return ((Score)object).getValue();
    }

    public void setValue(String value) {
		((Score)object).setValue(value);
    }

	// Property ScoreSource

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity = ProvenanceProxy.class)
	@JoinColumn(name="score_source_x")
    public Provenance getScoreSource() {
		return ((Score)object).getScoreSource();
    }

    public void setScoreSource(Provenance scoreSource) {
		((Score)object).setScoreSource(scoreSource);
    }
    
	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return Score.class;
	}
}
