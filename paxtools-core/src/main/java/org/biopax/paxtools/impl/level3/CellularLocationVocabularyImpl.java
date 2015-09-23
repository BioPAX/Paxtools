package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.CellularLocationVocabulary;


public class CellularLocationVocabularyImpl extends ControlledVocabularyImpl
        implements CellularLocationVocabulary 
{	
	public CellularLocationVocabularyImpl() {
	}

    public Class<? extends CellularLocationVocabulary> getModelInterface() {
        return CellularLocationVocabulary.class;
    }
}
