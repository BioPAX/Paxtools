package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.EvidenceCodeVocabulary;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 */
@Entity
@Indexed
public class EvidenceCodeVocabularyImpl extends ControlledVocabularyImpl
	implements EvidenceCodeVocabulary
{
    @Override @Transient
    public Class<? extends EvidenceCodeVocabulary> getModelInterface() {
        return EvidenceCodeVocabulary.class;
    }
}