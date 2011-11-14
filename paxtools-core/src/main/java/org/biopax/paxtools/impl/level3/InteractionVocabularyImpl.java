package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.InteractionVocabulary;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 */
@Entity
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class InteractionVocabularyImpl extends ControlledVocabularyImpl
	implements InteractionVocabulary
{
	public InteractionVocabularyImpl() {
	}
	
    @Override @Transient
    public Class<? extends InteractionVocabulary> getModelInterface() {
        return InteractionVocabulary.class;
    }
}