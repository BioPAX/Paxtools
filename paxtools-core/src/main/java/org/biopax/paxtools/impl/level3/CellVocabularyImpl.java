package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.CellVocabulary;

import javax.persistence.Entity;

/**
 */
@Entity
public class CellVocabularyImpl extends ControlledVocabularyImpl
	implements CellVocabulary
{
    @Override
    public Class<? extends CellVocabulary> getModelInterface() {
        return CellVocabulary.class;
    }
}