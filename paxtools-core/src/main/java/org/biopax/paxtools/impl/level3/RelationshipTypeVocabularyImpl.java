package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.RelationshipTypeVocabulary;


public class RelationshipTypeVocabularyImpl extends ControlledVocabularyImpl
        implements RelationshipTypeVocabulary {

	public RelationshipTypeVocabularyImpl() {
	}
	
    @Override
    public Class<? extends RelationshipTypeVocabulary> getModelInterface() {
        return RelationshipTypeVocabulary.class;
    }
}
