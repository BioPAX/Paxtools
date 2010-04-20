package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.SequenceModificationVocabulary;

import javax.persistence.Entity;

/**
 */

@Entity
public class SequenceModificationVocabularyImpl extends ControlledVocabularyImpl
	implements SequenceModificationVocabulary
{
    @Override
    public Class<? extends SequenceModificationVocabulary> getModelInterface() {
        return SequenceModificationVocabulary.class;
    }
}