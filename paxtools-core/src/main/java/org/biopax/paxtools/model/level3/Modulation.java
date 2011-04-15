package org.biopax.paxtools.model.level3;

/**
 * Definition: An interaction in which one entity regulates, modifies, or otherwise influences another. Two types of
 * control interactions are defined: activation and inhibition.
 * <p/>
 * Comment: The targets of control processes (i.e. values of the controlled property) should be Interactions or
 * Pathways, not physical entities. The physical entities are involved in processes,
 * which are controlled.  The physical entities are not themselves controlled. For example,
 * a kinase activating a protein is a frequent event in signaling pathways and is usually represented in signaling
 * diagrams using an ‘activation’ arrow from the kinase to the substrate. The problem with this is that the substrate
 * may not be active in other contexts. For this reason, BioPAX does not support these types of control or activation
 * flow networks. In BioPAX, this information should be captured as the kinase catalyzing (via an instance of the
 * Catalysis class) a reaction in which the substrate is phosphorylated.
 * <p/>
 * Synonyms: regulation, mediation
 * <p/>
 * Examples: A small molecule that inhibits a pathway by an unknown mechanism controls the pathway.
 * <p/>
 * Notes: Instances of Control can have multiple controller’s and controlled’s. Moreover,
 * one Control instance can control another Control instance. The semantics of the use of these properties are as
 * follows:
 * <p/>
 * Multiple separate controls controlling a conversion means that they control in parallel (e.g. different enzymes
 * catalyzing the same reaction). Generally, their effect on the rate of the reaction is cumulative.
 * <p/>
 * A control with multiple controllers indicates a dependency between these controllers,
 * typically meaning that both are required for the reaction to occur (e.g. a catalysis with an enzyme and a cofactor
 * as controllers). Any further chaining of controls also implies dependency, for example allosteric inhibition of
 * the aforementioned enzyme by a small molecule.
 * <p/>
 * Here is a pseudo-BioPAX representation of the examples above:
 * rxn1 is a BiochemicalReaction
 * <p/>
 * cat1 is a Catalysis
 * cat2 is a Catalysis
 * <p/>
 * mod1 is a Modulation
 * <p/>
 * enzyme1 is a Protein
 * enzyme2 is a Protein
 * <p/>
 * cofactor1 is a SmallMolecule
 * drug1 is a SmallMolecule
 * <p/>
 * cat1 has controlled rxn1
 * cat2 has controlled rxn1 (Both cat1 and cat2 can catalyze rxn1, independently)
 * <p/>
 * cat1 has controller enzyme1
 * <p/>
 * cat2 has controller enzyme2
 * cat2 has cofactor cofactor1 (both enzyme2 and cofactor1 is required for cat2 to occur)
 * <p/>
 * mod1 has controlled cat2
 * mod1 has control-type INHIBITION_ALLOSTERIC
 * mod1 has controller drug1 (drug1 should NOT be present for cat2 to occur)
 * <p/>
 * This structure is similar to disjunctive normal form (DNF) in Boolean logic. We could write this as: (enzyme1) OR
 * (enzyme2 AND cofactor1 AND NOT drug1)
 */

public interface Modulation extends Control
{

}
