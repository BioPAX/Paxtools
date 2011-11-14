package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.EntityReferenceTypeVocabulary;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 */
@Entity
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class EntityReferenceTypeVocabularyImpl extends ControlledVocabularyImpl
	implements EntityReferenceTypeVocabulary
{
	public EntityReferenceTypeVocabularyImpl() {
	}
	
    @Override @Transient
    public Class<? extends EntityReferenceTypeVocabulary> getModelInterface() {
        return EntityReferenceTypeVocabulary.class;
    }
}