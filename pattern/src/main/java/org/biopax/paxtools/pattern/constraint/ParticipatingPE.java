package org.biopax.paxtools.pattern.constraint;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.*;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.util.Blacklist;
import org.biopax.paxtools.pattern.util.RelType;

import java.util.*;

/**
 * This constraint is useful when we want traverse Control-Conversion-PE (input or output). Control
 * and Conversion are prerequisites, PE is generated.
 *
 * Var0 - Control
 * Var1 - Conversion
 * Var2 - input or output PhysicalEntity
 *
 * @deprecated
 * @see Participant
 *
 * @author Ozgun Babur
 */
public class ParticipatingPE extends ConstraintAdapter
{
	/**
	 * Desired direction after the Conversion.
	 */
	RelType type;

	/**
	 * Constructor with parameters.
	 * @param type direction from the Conversion
	 */
	public ParticipatingPE(RelType type, Blacklist blacklist)
	{
		super(3, blacklist);
		this.type = type;
	}

	/**
	 * Constructor with parameters.
	 * @param type direction from the Conversion
	 */
	public ParticipatingPE(RelType type)
	{
		this(type, null);
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
	 * First evaluates the direction that the Control is affecting the Conversion, then gets the
	 * related participants.
	 * @param match current pattern match
	 * @param ind mapped indices
	 * @return related participants
	 */
	@Override
	public Collection<BioPAXElement> generate(Match match, int... ind)
	{
		Control cont = (Control) match.get(ind[0]);
		Conversion conv = (Conversion) match.get(ind[1]);

		// This is the direction for our use only.
		ConversionDirectionType dir = getDirection(conv, cont);

		Set<Set<PhysicalEntity>> sides = new HashSet<Set<PhysicalEntity>>();

		if (dir == ConversionDirectionType.LEFT_TO_RIGHT)
		{
			sides.add(type == RelType.INPUT ? conv.getLeft() : conv.getRight());
		}
		else if (dir == ConversionDirectionType.RIGHT_TO_LEFT)
		{
			sides.add(type == RelType.OUTPUT ? conv.getLeft() : conv.getRight());
		}
		else // dir is reversible and we will go both sides
		{
			sides.add(conv.getLeft());
			sides.add(conv.getRight());
		}

		Set<BioPAXElement> result = new HashSet<BioPAXElement>();

		if (blacklist == null)
		{
			for (Set<PhysicalEntity> side : sides)
			{
				result.addAll(side);
			}
		}
		else
		{
			for (Set<PhysicalEntity> side : sides)
			{
				// if the control is in fact reversible then don't mind the context
				result.addAll(blacklist.getNonUbiques(side,
					dir == ConversionDirectionType.REVERSIBLE ? null : type));
			}
		}

		return result;
	}
}
