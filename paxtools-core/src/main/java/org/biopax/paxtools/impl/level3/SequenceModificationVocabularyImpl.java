package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.SequenceModificationVocabulary;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 */

@Entity
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class SequenceModificationVocabularyImpl extends ControlledVocabularyImpl
	implements SequenceModificationVocabulary
{
	public SequenceModificationVocabularyImpl() {
	}
	
    @Override @Transient
    public Class<? extends SequenceModificationVocabulary> getModelInterface() {
        return SequenceModificationVocabulary.class;
    }
}