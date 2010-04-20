package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.InteractionVocabulary;

import javax.persistence.Entity;

/**
 */
@Entity
public class InteractionVocabularyImpl extends ControlledVocabularyImpl
	implements InteractionVocabulary
{
    @Override
    public Class<? extends InteractionVocabulary> getModelInterface() {
        return InteractionVocabulary.class;
    }
}