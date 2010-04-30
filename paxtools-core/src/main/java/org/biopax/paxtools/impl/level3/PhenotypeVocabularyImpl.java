package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.PhenotypeVocabulary;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 */
@Entity
public class PhenotypeVocabularyImpl extends ControlledVocabularyImpl
	implements PhenotypeVocabulary
{
	private String patoData;

	@Override @Transient 
    public Class<? extends PhenotypeVocabulary> getModelInterface() {
        return PhenotypeVocabulary.class;
    }

	@Basic
	public String getPatoData()
	{
		return patoData;
	}

	public void setPatoData(String patoData)
	{
		this.patoData  = patoData;
	}


}