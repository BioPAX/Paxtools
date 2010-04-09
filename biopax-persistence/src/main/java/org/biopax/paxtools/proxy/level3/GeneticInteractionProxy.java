/*
 * GeneticInteractionProxy.java
 *
 * 2007.12.04 Takeshi Yoneki
 * INOH project - http://www.inoh.org
 */

package org.biopax.paxtools.proxy.level3;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.proxy.BioPAXElementProxy;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import javax.persistence.Entity;

/**
 * Proxy for GeneticInteraction
 */
@Entity(name="l3geneticinteraction")
@Indexed(index=BioPAXElementProxy.SEARCH_INDEX_NAME)
public class GeneticInteractionProxy extends InteractionProxy<GeneticInteraction> implements GeneticInteraction {
// GeneticInteraction

 	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity = PhenotypeVocabularyProxy.class)
	@JoinColumn(name="phenotype_x")
	public PhenotypeVocabulary getPhenotype() {
		return object.getPhenotype();
	}

    public void setPhenotype(PhenotypeVocabulary phenotype) {
		object.setPhenotype(phenotype);
    }


 	@ManyToOne(cascade = {CascadeType.ALL}, targetEntity = ScoreProxy.class)
	@JoinColumn(name="interaction_score_x")
    public Score getInteractionScore() {
		return object.getInteractionScore();
    }

    public void setInteractionScore(Score interactionScore) {
		object.setInteractionScore(interactionScore);
    }
    
    @Transient
	public Class<? extends BioPAXElement> getModelInterface() {
    	return GeneticInteraction.class;
    }
}
