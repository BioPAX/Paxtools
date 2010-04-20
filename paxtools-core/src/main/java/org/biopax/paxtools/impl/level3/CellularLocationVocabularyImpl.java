package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.CellularLocationVocabulary;

import javax.persistence.Entity;

/**
 */
@Entity
class CellularLocationVocabularyImpl extends ControlledVocabularyImpl
        implements CellularLocationVocabulary {

    @Override
    public Class<? extends CellularLocationVocabulary> getModelInterface() {
        return CellularLocationVocabulary.class;
    }
}
