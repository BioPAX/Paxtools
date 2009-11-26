/*
 * EvidenceCodeVocabularyProxy.java
 *
 * 2007.12.03 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.Serializable;

/**
 * Proxy for evidenceCodeVocabulary
 */
@Entity(name="l3evidencecodevocabulary")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class EvidenceCodeVocabularyProxy extends ControlledVocabularyProxy implements EvidenceCodeVocabulary, Serializable {
	public EvidenceCodeVocabularyProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return EvidenceCodeVocabulary.class;
	}
}
