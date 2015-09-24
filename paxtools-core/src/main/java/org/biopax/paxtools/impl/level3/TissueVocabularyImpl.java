package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.TissueVocabulary;


public class TissueVocabularyImpl extends ControlledVocabularyImpl
	implements TissueVocabulary
{
	public TissueVocabularyImpl() {
	}
	
    @Override
    public Class<? extends TissueVocabulary> getModelInterface() {
        return TissueVocabulary.class;
    }
}