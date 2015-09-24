package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.SequenceModificationVocabulary;


public class SequenceModificationVocabularyImpl extends ControlledVocabularyImpl
	implements SequenceModificationVocabulary
{
	public SequenceModificationVocabularyImpl() {
	}
	
    @Override
    public Class<? extends SequenceModificationVocabulary> getModelInterface() {
        return SequenceModificationVocabulary.class;
    }
}