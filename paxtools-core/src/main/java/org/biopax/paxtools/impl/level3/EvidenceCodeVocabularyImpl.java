package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.EvidenceCodeVocabulary;


public class EvidenceCodeVocabularyImpl extends ControlledVocabularyImpl
	implements EvidenceCodeVocabulary
{
	public EvidenceCodeVocabularyImpl() {
	}
	
    @Override
    public Class<? extends EvidenceCodeVocabulary> getModelInterface() {
        return EvidenceCodeVocabulary.class;
    }
}