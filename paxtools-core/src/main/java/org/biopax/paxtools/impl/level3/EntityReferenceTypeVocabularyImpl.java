package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.EntityReferenceTypeVocabulary;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 */
@Entity
public class EntityReferenceTypeVocabularyImpl extends ControlledVocabularyImpl
	implements EntityReferenceTypeVocabulary
{
    @Override @Transient
    public Class<? extends EntityReferenceTypeVocabulary> getModelInterface() {
        return EntityReferenceTypeVocabulary.class;
    }
}