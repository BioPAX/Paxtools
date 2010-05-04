package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.InteractionVocabulary;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 */
@Entity
@Indexed(index=BioPAXElementImpl.SEARCH_INDEX_FOR_UTILILTY_CLASS)
public class InteractionVocabularyImpl extends ControlledVocabularyImpl
	implements InteractionVocabulary
{
    @Override @Transient
    public Class<? extends InteractionVocabulary> getModelInterface() {
        return InteractionVocabulary.class;
    }
}