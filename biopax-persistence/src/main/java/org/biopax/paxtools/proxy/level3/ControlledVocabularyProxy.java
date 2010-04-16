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
public class ControlledVocabularyProxy<T extends ControlledVocabulary> extends XReferrableProxy<T>
	implements ControlledVocabulary 
{

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

