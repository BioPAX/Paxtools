package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.CellVocabulary;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class CellVocabularyImpl extends ControlledVocabularyImpl
	implements CellVocabulary
{
	public CellVocabularyImpl() {
	}

	
	@Override @Transient
    public Class<? extends CellVocabulary> getModelInterface() {
        return CellVocabulary.class;
    }
}