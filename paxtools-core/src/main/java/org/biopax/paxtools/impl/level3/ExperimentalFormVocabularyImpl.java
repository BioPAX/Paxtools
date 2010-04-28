package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.ExperimentalFormVocabulary;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 */
@Entity
public class ExperimentalFormVocabularyImpl extends ControlledVocabularyImpl
	implements ExperimentalFormVocabulary
{
    @Override @Transient
    public Class<? extends ExperimentalFormVocabulary> getModelInterface() {
        return ExperimentalFormVocabulary.class;
    }
}