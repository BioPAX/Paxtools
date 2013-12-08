package org.biopax.paxtools.pattern.constraint;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.pattern.Match;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Given Conversion and its one of the participants (at the left or right), traverses to either
 * the participants at the other side or the same side.
 *
 * var0 is a PE (PE1)
 * var1 is a Conv
 * var2 is a PE (PE2)
 *
 * Prerequisite: PE1 is either at left or right of Conv
 *
 * @author Ozgun Babur
 */
public class ConversionSide extends ConstraintAdapter
{
	Type sideType;

	/**
	 * Constructor.
	 */
	public ConversionSide(Type type)
	{
		super(3);

		if (type == null)
		{
			throw new IllegalArgumentException("The \"type\" parameter cannot be null.");
		}

		this.sideType = type;
	}

	/**
	 * This is a generative constraint.
	 * @return true
	 */
	@Override
	public boolean canGenerate()
	{
		return true;
	}

	/**
	 * Checks which side is the first PhysicalEntity, and gathers participants on either the other
	 * side or the same side.
	 * @param match current pattern match
	 * @param ind mapped indices
	 * @return participants at the desired side
	 */
	@Override
	public Collection<BioPAXElement> generate(Match match, int... ind)
	{
		assertIndLength(ind);

		PhysicalEntity pe1 = (PhysicalEntity) match.get(ind[0]);
		Conversion conv = (Conversion) match.get(ind[1]);

		if (conv.getLeft().contains(pe1))
		{
			return new HashSet<BioPAXElement>(
				sideType == Type.OTHER_SIDE ? conv.getRight() : conv.getLeft());
		}
		else if (conv.getRight().contains(pe1))
		{
			return new HashSet<BioPAXElement>(
				sideType == Type.SAME_SIDE ? conv.getRight() : conv.getLeft());
		}
		return Collections.emptySet();
	}

	/**
	 * This enum tells if the user want to traverse towards other side of the conversion or stay at
	 * the same side.
	 */
	public enum Type
	{
		OTHER_SIDE,
		SAME_SIDE
	}
}
