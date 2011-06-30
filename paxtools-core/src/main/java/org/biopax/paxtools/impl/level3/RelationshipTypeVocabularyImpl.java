package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.RelationshipTypeVocabulary;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class RelationshipTypeVocabularyImpl extends ControlledVocabularyImpl
        implements RelationshipTypeVocabulary {

	public RelationshipTypeVocabularyImpl() {
	}
	
    @Override @Transient
    public Class<? extends RelationshipTypeVocabulary> getModelInterface() {
        return RelationshipTypeVocabulary.class;
    }
}
