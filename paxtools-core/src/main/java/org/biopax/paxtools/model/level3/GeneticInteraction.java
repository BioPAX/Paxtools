package org.biopax.paxtools.model.level3;

/**
 * Definition : Genetic interactions between genes occur when two genetic perturbations (e.g. mutations) have a
 * combined phenotypic effect not caused by either perturbation alone. A gene participant in a genetic interaction
 * represents the gene that is perturbed. Genetic interactions are not physical interactions but logical (AND)
 * relationships. Their physical manifestations can be complex and span an arbitarily long duration.
 * <p/>
 * Rationale: Currently,  BioPAX provides a simple definition that can capture most genetic interactions described in
 * the literature. In the future, if required, the definition can be extended to capture other logical relationships
 * and different, participant specific phenotypes.
 * <p/>
 * Example: A synthetic lethal interaction occurs when cell growth is possible without either gene A OR B,
 * but not without both gene A AND B. If you knock out A and B together, the cell will die.
 */
public interface GeneticInteraction extends Interaction
{

	/**
	 * @return The phenotype quality used to define this genetic interaction e.g. viability.
	 */
	PhenotypeVocabulary getPhenotype();

	/**
	 * @param phenotype The phenotype quality used to define this genetic interaction e.g. viability.
	 */
	void setPhenotype(PhenotypeVocabulary phenotype);

	/**
	 * @return The score of an interaction e.g. a genetic interaction score.
	 */
	Score getInteractionScore();

	/**
	 * @param interactionScore The score of an interaction e.g. a genetic interaction score.
	 */
	void setInteractionScore(Score interactionScore);
}
