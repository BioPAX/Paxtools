package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.EntityReferenceTypeVocabulary;


public class EntityReferenceTypeVocabularyImpl extends ControlledVocabularyImpl
	implements EntityReferenceTypeVocabulary
{
	public EntityReferenceTypeVocabularyImpl() {
	}
	
    @Override
    public Class<? extends EntityReferenceTypeVocabulary> getModelInterface() {
        return EntityReferenceTypeVocabulary.class;
    }
}