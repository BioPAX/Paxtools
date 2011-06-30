package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.SequenceRegionVocabulary;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 */
@Entity
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class SequenceRegionVocabularyImpl extends ControlledVocabularyImpl
	implements SequenceRegionVocabulary
{
	public SequenceRegionVocabularyImpl() {
	}
	
    @Override @Transient
    public Class<? extends SequenceRegionVocabulary> getModelInterface() {
        return SequenceRegionVocabulary.class;
    }
}