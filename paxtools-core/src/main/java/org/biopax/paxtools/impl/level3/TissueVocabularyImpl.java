package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.TissueVocabulary;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 */
@Entity
@Indexed
public class TissueVocabularyImpl extends ControlledVocabularyImpl
	implements TissueVocabulary
{
    @Override @Transient
    public Class<? extends TissueVocabulary> getModelInterface() {
        return TissueVocabulary.class;
    }
}