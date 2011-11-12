package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.TissueVocabulary;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class TissueVocabularyImpl extends ControlledVocabularyImpl
	implements TissueVocabulary
{
	public TissueVocabularyImpl() {
	}
	
    @Override @Transient
    public Class<? extends TissueVocabulary> getModelInterface() {
        return TissueVocabulary.class;
    }
}