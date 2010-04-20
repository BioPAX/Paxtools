package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.TissueVocabulary;

import javax.persistence.Entity;

/**
 */
@Entity
public class TissueVocabularyImpl extends ControlledVocabularyImpl
	implements TissueVocabulary
{
    @Override
    public Class<? extends TissueVocabulary> getModelInterface() {
        return TissueVocabulary.class;
    }
}