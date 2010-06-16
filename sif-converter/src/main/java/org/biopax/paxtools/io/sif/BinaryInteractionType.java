package org.biopax.paxtools.io.sif;

/**
 * This class holds the description, tag (used in SIF file), the information if the interaction is
 * directed for the relevant binary interaction types.
 */
public enum BinaryInteractionType
{

	COMPONENT_OF(
			"The first entity is a component of the second entity, which is a complex.  " +
			"This interaction is transient in the sense that A component_of B and B component_of " +
			"C implies A component_of C.  " +
			"This interaction is directed.", true),

	IN_SAME_COMPONENT(
			"Two entities belong to the same molecular complex.  This does not necessarily " +
			"mean they interact directly.  In a complex with n molecules, this rule will create a " +
			"clique composed of n(n-1)/2 interactions.  " +
			"This interaction is undirected.", false),

	CO_CONTROL(
			"This rule infers an interaction " +
			"if the first and second entities have control over the same process. " +
			"This interaction is undirected.", false),

	SEQUENTIAL_CATALYSIS(
			"The entities catalyze two conversions that are connected via a common molecule, " +
			"e.g. the first entity produces a substrate that is consumed by the second entity.  " +
			"This interaction is directed.", true),

	METABOLIC_CATALYSIS(
			"The first entity catalyzes a reaction that either consumes or produces the second entity.  " +
			"This interaction is directed.", true),

	STATE_CHANGE(
			"The first entity controls a reaction that changes the state of the " +
			"second entity, e.g. by phosphorylation or other posttranslational modification, " +
			"or by a change in subcellular location.  " +
			"This interaction is directed.", true),

	ACTIVATES(
			"The first entity controls a reaction that changes the state of the " +
			"second entity, from inactive or notr, to active. " +
			"This interaction is directed.", true),

	INACTIVATES(
			"The first entity controls a reaction that changes the state of the " +
			"second entity, from active or notr, to inactive. " +
			"This interaction is directed.", true),

	REACTS_WITH(
			"The entities participate in a conversion as substrates or products.  Controllers are not included.  " +
			"This interaction is undirected.", false),

	INTERACTS_WITH(
			"The entities participate in an interaction.  Controllers are not included.  " +
			"This interaction is undirected.", false);

	private String description;
	private boolean directed;

	BinaryInteractionType(
			String description,
			boolean directed)
	{
		this.description = description;
		this.directed = directed;
	}

	/**
	 * Returns the description of the binary interaction as a string.
	 *
	 * @return a string describing the interaction
	 */
	public String getDescription()
	{
		return description;
	}

	/**
	 * Returns the tag of the binary interaction. This tag is used in the SIF file(s).
	 *
	 * @return tag name as it is used in SIF files
	 */
	public String getTag()
	{
		return this.toString();
	}

	/**
	 * Returns true, if the interaction is directed.
	 *
	 * @return false, if the interaction is not directed.
	 */
	public boolean isDirected()
	{
		return directed;
	}
}
