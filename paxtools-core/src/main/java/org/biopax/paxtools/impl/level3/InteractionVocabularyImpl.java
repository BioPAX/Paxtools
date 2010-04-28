package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.InteractionVocabulary;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 */
@Entity
public class InteractionVocabularyImpl extends ControlledVocabularyImpl
	implements InteractionVocabulary
{
    @Override @Transient
    public Class<? extends InteractionVocabulary> getModelInterface() {
        return InteractionVocabulary.class;
    }
}