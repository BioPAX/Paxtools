/*
 * SequenceRegionVocabularyProxy.java
 *
 * 2007.12.04 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.Serializable;

/**
 * Proxy for SequenceRegionVocabulary
 */
@Entity(name="l3sequencelocationvocabulary")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class SequenceRegionVocabularyProxy extends ControlledVocabularyProxy implements SequenceRegionVocabulary, Serializable {
	public SequenceRegionVocabularyProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return SequenceRegionVocabulary.class;
	}
}
