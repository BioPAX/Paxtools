package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.RelationshipTypeVocabulary;

/**
 * TODO:Class description
 * User: demir
 * Date: Aug 14, 2008
 * Time: 7:31:03 PM
 */
public class RelationshipTypeVocabularyImpl extends ControlledVocabularyImpl
        implements RelationshipTypeVocabulary {

    @Override
    public Class<? extends RelationshipTypeVocabulary> getModelInterface() {
        return RelationshipTypeVocabulary.class;
    }
}
