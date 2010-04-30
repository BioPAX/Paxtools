package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.RelationshipTypeVocabulary;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
public class RelationshipTypeVocabularyImpl extends ControlledVocabularyImpl
        implements RelationshipTypeVocabulary {

    @Override @Transient
    public Class<? extends RelationshipTypeVocabulary> getModelInterface() {
        return RelationshipTypeVocabulary.class;
    }
}
