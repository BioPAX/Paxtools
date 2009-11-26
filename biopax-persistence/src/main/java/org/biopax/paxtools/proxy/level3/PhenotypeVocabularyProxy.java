/*
 * PhenotypeVocabularyProxy.java
 *
 * 2008.02.27 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import java.io.Serializable;

/**
 * Proxy for PhenotypeVocabulary
 */
@Entity(name="l3phenotypevocabulary")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class PhenotypeVocabularyProxy extends ControlledVocabularyProxy implements PhenotypeVocabulary, Serializable {
	public PhenotypeVocabularyProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return PhenotypeVocabulary.class;
	}

	// PatoData

	@Basic @Column(name="pato_data_x", columnDefinition="text")
	@Field(name=BioPAXElementProxy.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
	public String getPatoData() {
		return ((PhenotypeVocabulary)object).getPatoData();
	}

	public void setPatoData(String patoData) {
		((PhenotypeVocabulary)object).setPatoData(patoData);
	}
}
