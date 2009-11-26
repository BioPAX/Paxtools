/*
 * GeneticInteractionProxy.java
 *
 * 2007.12.04 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.level3.*;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;
import java.io.Serializable;

/**
 * Proxy for GeneticInteraction
 */
@Entity(name="l3geneticinteraction")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class GeneticInteractionProxy extends InteractionProxy implements GeneticInteraction, Serializable {
	public GeneticInteractionProxy() {
	}

	@Transient
	public Class getModelInterface() {
		return GeneticInteraction.class;
	}

// GeneticInteraction

 	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity = PhenotypeVocabularyProxy.class)
	@JoinColumn(name="phenotype_x")
	public PhenotypeVocabulary getPhenotype() {
		return ((GeneticInteraction)object).getPhenotype();
	}

    public void setPhenotype(PhenotypeVocabulary phenotype) {
		((GeneticInteraction)object).setPhenotype(phenotype);
    }


 	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity = ScoreProxy.class)
	@JoinColumn(name="interaction_score_x")
    public Score getInteractionScore() {
		return ((GeneticInteraction)object).getInteractionScore();
    }

    public void setInteractionScore(Score interactionScore) {
		((GeneticInteraction)object).setInteractionScore(interactionScore);
    }
}
