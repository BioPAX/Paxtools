package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.ExperimentalFormVocabulary;

/**
 */
public class ExperimentalFormVocabularyImpl extends ControlledVocabularyImpl
	implements ExperimentalFormVocabulary
{
    @Override
    public Class<? extends ExperimentalFormVocabulary> getModelInterface() {
        return ExperimentalFormVocabulary.class;
    }
}