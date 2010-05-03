package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.RelationshipTypeVocabulary;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@Indexed
public class RelationshipTypeVocabularyImpl extends ControlledVocabularyImpl
        implements RelationshipTypeVocabulary {

    @Override @Transient
    public Class<? extends RelationshipTypeVocabulary> getModelInterface() {
        return RelationshipTypeVocabulary.class;
    }
}
