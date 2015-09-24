package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.PhenotypeVocabulary;


public class PhenotypeVocabularyImpl extends ControlledVocabularyImpl
	implements PhenotypeVocabulary
{
	private String patoData;

	public PhenotypeVocabularyImpl() {
	}
	
	@Override
    public Class<? extends PhenotypeVocabulary> getModelInterface() {
        return PhenotypeVocabulary.class;
    }

	public String getPatoData()
	{
		return patoData;
	}

	public void setPatoData(String patoData)
	{
		this.patoData  = patoData;
	}

}