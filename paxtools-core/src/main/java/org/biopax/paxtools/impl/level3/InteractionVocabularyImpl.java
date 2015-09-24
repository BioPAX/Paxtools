package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.InteractionVocabulary;


public class InteractionVocabularyImpl extends ControlledVocabularyImpl
	implements InteractionVocabulary
{
	public InteractionVocabularyImpl() {
	}
	
    @Override
    public Class<? extends InteractionVocabulary> getModelInterface() {
        return InteractionVocabulary.class;
    }
}