package org.biopax.paxtools.pattern.constraint;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.Match;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * This constraint is used to collect related EntityReference of the participant physical entities.
 * The constraint let's users to set some participants as taboo, and they are excluded from
 * analysis.
 *
 * Var0 - Interaction
 * Var1 - Taboo element number 1
 * ...
 * Var(numTabooElements+1) - Last taboo element
 * Var(numTabooElements+2) - The related EntityReference
 *
 * @author Ozgun Babur
 */
public class InterToPartER extends ConstraintAdapter
{
	/**
	 * Direction to go. When this parameter is used, the interaction has to be a Conversion.
	 */
	private Direction direction;

	/**
	 * Constraint used for traversing towards simpler PE.
	 */
	private static final LinkedPE linker = new LinkedPE(LinkedPE.Type.TO_SPECIFIC);

	/**
	 * Accessor from PE to ER.
	 */
	private static final PathAccessor pe2ER =
		new PathAccessor("SimplePhysicalEntity/entityReference");

	/**
	 * Constructor with parameters. A taboo element is the participant that we want to exclude from
	 * the analysis. User should provide the number of taboo elements, then during execution, these
	 * elements will be fetched from the current match.
	 *
	 * @param numOfTabooElements the number of entities to exclude from the analysis
	 */
	public InterToPartER(int numOfTabooElements)
	{
		super(numOfTabooElements + 2);
	}

	/**
	 * Constructor with parameters. A taboo element is the participant that we want to exclude from
	 * the analysis. User should provide the number of taboo elements, then during execution, these
	 * elements will be fetched from the current match. The direction is left, or right, or both
	 * sides of the Conversion.
	 *
	 * @param numOfTabooElements the number of entities to exclude from the analysis
	 */
	public InterToPartER(Direction direction, int numOfTabooElements)
	{
		this(numOfTabooElements);
		this.direction = direction;
	}

	/**
	 * Constructor without parameters. There are no taboo elements.
	 */
	public InterToPartER()
	{
		this(0);
	}

	/**
	 * Constructor with direction. There are no taboo elements.
	 *
	 * @param direction - side(s) of an interaction to consider;
	 *                    see {@link org.biopax.paxtools.pattern.constraint.InterToPartER.Direction}
	 */
	public InterToPartER(Direction direction)
	{
		this();
		this.direction = direction;
	}

	/**
	 * This is a generative constraint.
	 * @return true if the constraint can generate candidates
	 */
	@Override
	public boolean canGenerate()
	{
		return true;
	}

	/**
	 * Iterated over non-taboo participants and collectes related ER.
	 * @param match current pattern match
	 * @param ind mapped indices
	 * @return related participants
	 */
	@Override
	public Collection<BioPAXElement> generate(Match match, int... ind)
	{
		Interaction inter = (Interaction) match.get(ind[0]);

		Set<Entity> taboo = new HashSet<Entity>();

		for (int i = 1; i < getVariableSize() - 1; i++)
		{
			taboo.add((Entity) match.get(ind[i]));
		}

		if (direction == null) return generate(inter, taboo);
		else return generate((Conversion) inter, direction, taboo);
	}

	/**
	 * Gets the related entity references of the given interaction.
	 * @param inter interaction
	 * @param taboo entities to ignore/skip
	 * @return entity references
	 */
	protected Collection<BioPAXElement> generate(Interaction inter, Set<Entity> taboo)
	{
		Set<BioPAXElement> simples = new HashSet<BioPAXElement>();

		for (Entity part : inter.getParticipant())
		{
			if (part instanceof PhysicalEntity && !taboo.contains(part))
			{
				simples.addAll(linker.getLinkedElements((PhysicalEntity) part));
			}
		}

		return pe2ER.getValueFromBeans(simples);
	}

	/**
	 * Gets the related entity references of the given interaction,
	 * @param conv conversion interaction
	 * @param direction which side(s) participants of the conversion to consider
	 * @param taboo skip list of entities
	 * @return entity references
	 */
	protected Collection<BioPAXElement> generate(Conversion conv, Direction direction,
		Set<Entity> taboo)
	{
		if (direction == null) throw new IllegalArgumentException("Direction cannot be null");

		if (!(direction == Direction.BOTHSIDERS || direction == Direction.ONESIDERS))
		{
			Set<BioPAXElement> simples = new HashSet<BioPAXElement>();

			for (Entity part : direction == Direction.ANY ? conv.getParticipant() :
				direction == Direction.LEFT ? conv.getLeft() : conv.getRight())
			{
				if (part instanceof PhysicalEntity && !taboo.contains(part))
				{
					simples.addAll(linker.getLinkedElements((PhysicalEntity) part));
				}
			}

			return pe2ER.getValueFromBeans(simples);
		}
		else
		{
			Set<BioPAXElement> leftSimples = new HashSet<BioPAXElement>();
			Set<BioPAXElement> rightSimples = new HashSet<BioPAXElement>();

			for (PhysicalEntity pe : conv.getLeft())
			{
				if (!taboo.contains(pe)) leftSimples.addAll(linker.getLinkedElements(pe));
			}
			for (PhysicalEntity pe : conv.getRight())
			{
				if (!taboo.contains(pe)) rightSimples.addAll(linker.getLinkedElements(pe));
			}

			Set leftERs = pe2ER.getValueFromBeans(leftSimples);
			Set rightERs = pe2ER.getValueFromBeans(rightSimples);

			if (direction == Direction.ONESIDERS)
			{
				// get all but intersection
				Set temp = new HashSet(leftERs);
				leftERs.removeAll(rightERs);
				rightERs.removeAll(temp);
				leftERs.addAll(rightERs);
			}
			else // BOTHSIDERS
			{
				// get intersection
				leftERs.retainAll(rightERs);
			}

			return leftERs;
		}
	}

	public enum Direction
	{
		LEFT,
		RIGHT,
		ANY,
		ONESIDERS,
		BOTHSIDERS
	}
}
