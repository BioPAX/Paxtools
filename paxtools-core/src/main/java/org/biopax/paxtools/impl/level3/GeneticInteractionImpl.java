package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.model.level3.GeneticInteraction;
import org.biopax.paxtools.model.level3.PhenotypeVocabulary;
import org.biopax.paxtools.model.level3.Score;

/**
 * Created by IntelliJ IDEA.
 * User: Emek
 * Date: Feb 22, 2008
 * Time: 10:22:12 AM
 */
class GeneticInteractionImpl extends InteractionImpl
        implements GeneticInteraction
{

	public Class<? extends GeneticInteraction> getModelInterface()
	{
		return GeneticInteraction.class;
	}
	private PhenotypeVocabulary phenotype;

    private Score interactionScore;

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
