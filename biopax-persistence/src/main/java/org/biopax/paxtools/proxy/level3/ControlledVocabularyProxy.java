/*
 * ControlledVocabularyProxy.java
 *
 * 2008.02.26 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */
package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import java.util.Set;

import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.biopax.paxtools.proxy.StringSetBridge;

/**
 * Proxy for openControlledVocabulary
 */
@Entity(name = "l3controlledvocabulary")
@Indexed(index = BioPAXElementProxy.SEARCH_INDEX_NAME)
public class ControlledVocabularyProxy<T extends ControlledVocabulary> extends Level3ElementProxy<T> 
	implements ControlledVocabulary 
{

// XReferrable
	@ManyToMany(cascade = {CascadeType.ALL}, targetEntity = XrefProxy.class)
	@JoinTable(name = "l3controlledvocabulary_xref")
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

// ControlledVocabulary

	// Property TERM
	@CollectionOfElements
	@Column(name = "term_x", columnDefinition = "text")
	@FieldBridge(impl = StringSetBridge.class)
	@Field(name = BioPAXElementProxy.SEARCH_FIELD_TERM, index = Index.TOKENIZED)
	public Set<String> getTerm() {
		return object.getTerm();
	}

	public void addTerm(String TERM) {
		object.addTerm(TERM);
	}

	public void removeTerm(String TERM) {
		object.removeTerm(TERM);
	}

	public void setTerm(Set<String> TERM) {
		object.setTerm(TERM);
	}

	@Transient
	public Class<? extends BioPAXElement> getModelInterface() {
		return CellVocabulary.class;
	}
}

