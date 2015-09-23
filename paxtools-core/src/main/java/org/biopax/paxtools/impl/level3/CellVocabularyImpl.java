package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.CellVocabulary;


public class CellVocabularyImpl extends ControlledVocabularyImpl
	implements CellVocabulary
{
	public CellVocabularyImpl() {
	}

    public Class<? extends CellVocabulary> getModelInterface() {
        return CellVocabulary.class;
    }
}