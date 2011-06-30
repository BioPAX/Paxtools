package org.biopax.paxtools.impl.level3;

import org.biopax.paxtools.impl.BioPAXElementImpl;
import org.biopax.paxtools.model.level3.GeneticInteraction;
import org.biopax.paxtools.model.level3.PhenotypeVocabulary;
import org.biopax.paxtools.model.level3.Score;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 */
@Entity
@Indexed//(index=BioPAXElementImpl.SEARCH_INDEX_NAME)
@org.hibernate.annotations.Entity(dynamicUpdate = true, dynamicInsert = true)
public class GeneticInteractionImpl extends InteractionImpl
        implements GeneticInteraction
{

	public GeneticInteractionImpl() {
	}
	
	@Transient
	public Class<? extends GeneticInteraction> getModelInterface()
	{
		return GeneticInteraction.class;
	}

	private PhenotypeVocabulary phenotype;

    private Score interactionScore;

    @ManyToOne(targetEntity = PhenotypeVocabularyImpl.class)//, cascade = {CascadeType.ALL})
	public PhenotypeVocabulary getPhenotype()
    {
        return phenotype;
    }

    public void setPhenotype(PhenotypeVocabulary phenotype)
    {
        this.phenotype = phenotype;
    }

	@ManyToOne(targetEntity = ScoreImpl.class)//, cascade={CascadeType.ALL})
    public Score getInteractionScore()
    {
        return interactionScore;
    }

    public void setInteractionScore(Score interactionScore)
    {
        this.interactionScore = interactionScore;
    }
}
