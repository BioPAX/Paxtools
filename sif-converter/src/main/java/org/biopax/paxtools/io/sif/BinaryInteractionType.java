package org.biopax.paxtools.io.sif;

import java.util.Map;
import java.util.HashMap;

import org.biopax.paxtools.model.BioPAXLevel;

/**
 * This class holds the description, tag (used in SIF file), the information if the interaction is
 * directed for the relevant binary interaction types.
 */
public enum BinaryInteractionType
{
	/**
	 * Returns a map between the new binary interaction tags and old ones. That is, new tag is key, old
	 * tag is value.
	 *
	 * @return Map<String, String>
	 */


	COMPONENT_OF(
			"COMPONENT_OF",
			"The first entity is a component of the second entity, which is a complex.  " +
			"This interaction is transient in the sense that A component_of B and B component_of " +
			"C implies A component_of C.  " +
			"This interaction is directed.", false, BioPAXLevel.L2),

	IN_SAME_COMPONENT(
			"COMPONENT_IN_SAME",
			"Two entities belong to the same molecular complex.  This does not necessarily " +
			"mean they interact directly.  In a complex with n molecules, this rule will create a " +
			"clique composed of n(n-1)/2 interactions.  " +
			"This interaction is directed.", true, BioPAXLevel.L2),

	CO_CONTROL_DEPENDENT_SIMILAR(
			"CO_CONTROL_DEPENDENT_SIMILAR",
			"This rule infers an interaction " +
			"if the first and second entities have control over the same process, " +
			"their control is dependent, i.e. one of them have effect over control of the other one, " +
			"and their effect is in the same direction (both activates or both inhibits).  " +
			"This interaction is undirected.", false, BioPAXLevel.L2),

	CO_CONTROL_DEPENDENT_ANTI(
			"CO_CONTROL_DEPENDENT_ANTI",
			"This rule infers an interaction " +
			"if the first and second entities have control over the same process, " +
			"their control is dependent, i.e. one of them have effect over control of the other one, " +
			"and their effect is in different directions (one of them activates, the other inhibits).  " +
			"This interaction is undirected.", false, BioPAXLevel.L2),

	CO_CONTROL_INDEPENDENT_SIMILAR(
			"CO_CONTROL_INDEPENDENT_SIMILAR",
			"This rule infers an " +
			"interaction if the first and secoutand entities have control over the same process, " +
			"their control is independent, i.e. they act without affecting each other's activity, " +
			"and their effect is in the same direction (both activates or both inhibits).  " +
			"This interaction is undirected.", false, BioPAXLevel.L2),

	CO_CONTROL_INDEPENDENT_ANTI(
			"CO_CONTROL_INDEPENDENT_ANTI",
			"This rule infers an " +
			"interaction if the first and second entities have control over the same process, " +
			"their control is independent, i.e. they act without affecting each other's activity, " +
			"and their effect is in different directions (one of them activates, the other inhibits).  " +
			"This interaction is undirected.", false, BioPAXLevel.L2),

	SEQUENTIAL_CATALYSIS(
			"SEQUENTIAL_CATALYSIS",
			"The entities catalyze two conversions that are connected via a common molecule, " +
			"e.g. the first entity produces a substrate that is consumed by the second entity.  " +
			"This interaction is directed.", true, BioPAXLevel.L2),

	METABOLIC_CATALYSIS(
			"CONTROLS_METABOLIC_CHANGE",
			"The first entity catalyzes a reaction that either consumes or produces the second entity.  " +
			"This interaction is directed.", true, BioPAXLevel.L2),

	STATE_CHANGE(
			"CONTROLS_STATE_CHANGE",
			"The first entity controls a reaction that changes the state of the " +
			"second entity, e.g. by phosphorylation or other posttranslational modification, " +
			"or by a change in subcellular location.  " +
			"This interaction is directed.", true, BioPAXLevel.L2),

	REACTS_WITH(
			"PARTICIPATES_CONVERSION",
			"The entities participate in a conversion as substrates or products.  Controllers are not included.  " +
			"This interaction is undirected.", false, BioPAXLevel.L2),

	INTERACTS_WITH(
			"PARTICIPATES_INTERACTION",
			"The entities participate in an interaction.  Controllers are not included.  " +
			"This interaction is undirected.", false, BioPAXLevel.L2);


	private String oldtag;
	private String description;
	private boolean directed;
	private BioPAXLevel level;


	BinaryInteractionType(
			String oldtag,
			String description,
			boolean directed,
			BioPAXLevel level)
	{
		this.oldtag = oldtag;
		this.description = description;
		this.directed = directed;
		this.level = level;
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

	/**
	 * Returns the BioPAX level to which the interaction belongs to.
	 *
	 * @return BioPAX level that the interaction is defined for.
	 */
	public BioPAXLevel getLevel()
	{
		return level;
	}


	public String getOldtag()
	{
		return oldtag;
	}
}
