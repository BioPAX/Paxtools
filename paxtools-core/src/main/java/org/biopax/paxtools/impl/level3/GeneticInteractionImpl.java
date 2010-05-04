package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.GeneticInteraction;
import org.biopax.paxtools.model.level3.PhenotypeVocabulary;
import org.biopax.paxtools.model.level3.Score;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

/**
 */
@Entity
@Indexed(index=BioPAXElementImpl.SEARCH_INDEX_FOR_ENTITY)
class GeneticInteractionImpl extends InteractionImpl
        implements GeneticInteraction
{

	@Transient
	public Class<? extends GeneticInteraction> getModelInterface()
	{
		return GeneticInteraction.class;
	}

	private PhenotypeVocabulary phenotype;

    private Score interactionScore;

    @ManyToOne(targetEntity = PhenotypeVocabularyImpl.class)
	public PhenotypeVocabulary getPhenotype()
    {
        return phenotype;
    }

    public void setPhenotype(PhenotypeVocabulary phenotype)
    {
        this.phenotype = phenotype;
    }

	@OneToOne(targetEntity = ScoreImpl.class)
    public Score getInteractionScore()
    {
        return interactionScore;
    }

    public void setInteractionScore(Score interactionScore)
    {
        this.interactionScore = interactionScore;
    }
}
