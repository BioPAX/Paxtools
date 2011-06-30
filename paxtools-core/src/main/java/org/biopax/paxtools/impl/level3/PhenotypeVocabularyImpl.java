package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.PhenotypeVocabulary;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 */
@Entity
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class PhenotypeVocabularyImpl extends ControlledVocabularyImpl
	implements PhenotypeVocabulary
{
	private String patoData;

	public PhenotypeVocabularyImpl() {
	}
	
	@Override @Transient 
    public Class<? extends PhenotypeVocabulary> getModelInterface() {
        return PhenotypeVocabulary.class;
    }

	
	@Field(name=BioPAXElementImpl.SEARCH_FIELD_KEYWORD, index=Index.TOKENIZED)
	public String getPatoData()
	{
		return patoData;
	}

	public void setPatoData(String patoData)
	{
		this.patoData  = patoData;
	}


}