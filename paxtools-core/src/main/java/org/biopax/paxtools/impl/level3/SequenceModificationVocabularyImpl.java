package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.SequenceModificationVocabulary;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 */

@Entity
@Indexed
public class SequenceModificationVocabularyImpl extends ControlledVocabularyImpl
	implements SequenceModificationVocabulary
{
    @Override @Transient
    public Class<? extends SequenceModificationVocabulary> getModelInterface() {
        return SequenceModificationVocabulary.class;
    }
}