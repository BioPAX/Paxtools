package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.EntityReferenceTypeVocabulary;

/**
 */
public class EntityReferenceTypeVocabularyImpl extends ControlledVocabularyImpl
	implements EntityReferenceTypeVocabulary
{
    @Override
    public Class<? extends EntityReferenceTypeVocabulary> getModelInterface() {
        return EntityReferenceTypeVocabulary.class;
    }
}