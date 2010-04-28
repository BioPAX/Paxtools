package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.CellVocabulary;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 */
@Entity
class CellVocabularyImpl extends ControlledVocabularyImpl
	implements CellVocabulary
{
    @Override @Transient
    public Class<? extends CellVocabulary> getModelInterface() {
        return CellVocabulary.class;
    }
}