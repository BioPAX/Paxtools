/*
 * SequenceModificationVocabularyProxy.java
 *
 * 2007.12.03 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;

/**
 * Proxy for SequenceModificationVocabulary
 */
@Entity(name="l3sequencemodificationvocab")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class SequenceModificationVocabularyProxy extends ControlledVocabularyProxy 
	implements SequenceModificationVocabulary
{
	public SequenceModificationVocabularyProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return SequenceModificationVocabulary.class;
	}
}
