package org.biopax.paxtools.model.level3;

/**
 * User: demir Date: Oct 8, 2007 Time: 6:47:42 PM
 */
public interface GeneticInteraction extends Interaction
{

    PhenotypeVocabulary getPhenotype();

    void setPhenotype(PhenotypeVocabulary phenotype);


    Score getInteractionScore();

    void setInteractionScore(Score interactionScore);
}
