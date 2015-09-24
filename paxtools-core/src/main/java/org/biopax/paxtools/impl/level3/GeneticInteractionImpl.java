package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.GeneticInteraction;
import org.biopax.paxtools.model.level3.PhenotypeVocabulary;
import org.biopax.paxtools.model.level3.Score;

public class GeneticInteractionImpl extends InteractionImpl
        implements GeneticInteraction
{
	private PhenotypeVocabulary phenotype;

    private Score interactionScore;
	
	public GeneticInteractionImpl() {
	}

	public Class<? extends GeneticInteraction> getModelInterface()
	{
		return GeneticInteraction.class;
	}

	public PhenotypeVocabulary getPhenotype()
    {
        return phenotype;
    }

    public void setPhenotype(PhenotypeVocabulary phenotype)
    {
        this.phenotype = phenotype;
    }

    public Score getInteractionScore()
    {
        return interactionScore;
    }

    public void setInteractionScore(Score interactionScore)
    {
        this.interactionScore = interactionScore;
    }
}
