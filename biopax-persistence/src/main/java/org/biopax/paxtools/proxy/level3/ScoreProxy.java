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
public class ScoreProxy extends Level3ElementProxy<Score> implements Score 
{
// XReferrable

	// Property Xref

	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = XrefProxy.class)
	@JoinTable(name="l3score_xref")
	public Set<Xref> getXref() {
		return object.getXref();
	}

	public void addXref(Xref XREF) {
		object.addXref(XREF);
	}

	public void removeXref(Xref XREF) {
		object.removeXref(XREF);
	}

	public void setXref(Set<Xref> XREF) {
		object.setXref(XREF);
	}

	@Basic @Column(name="value_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
    public String getValue() {
		return object.getValue();
    }

    public void setValue(String value) {
		object.setValue(value);
    }

	// Property ScoreSource

	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity = ProvenanceProxy.class)
	@JoinColumn(name="score_source_x")
    public Provenance getScoreSource() {
		return object.getScoreSource();
    }

    public void setScoreSource(Provenance scoreSource) {
		object.setScoreSource(scoreSource);
    }
    
	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return Score.class;
	}
}
