package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.PhenotypeVocabulary;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Proxy;
import org.hibernate.search.annotations.Boost;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 */
@Entity
@Proxy(proxyClass= PhenotypeVocabulary.class)
@Indexed
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
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

	
	@Field(name=FIELD_KEYWORD, index=Index.TOKENIZED)
	@Boost(1.1f)
	public String getPatoData()
	{
		return patoData;
	}

	public void setPatoData(String patoData)
	{
		this.patoData  = patoData;
	}


}