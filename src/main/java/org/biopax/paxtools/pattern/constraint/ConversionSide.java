package org.biopax.paxtools.pattern.constraint;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.ConversionDirectionType;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.util.Blacklist;
import org.biopax.paxtools.pattern.util.RelType;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
	/**
	 * Same side or other side;
	 */
	Type sideType;

	/**
	 * Type of the relation of the participant. It is either input or output.
	 */
	RelType relType;

	/**
	 * Constructor.
	 */
	public ConversionSide(Type type)
	{
		this(type, null, null);
	}

	/**
	 * Constructor. The relType parameter is using during blacklisting, and only if the blacklist is
	 * not null.
	 */
	public ConversionSide(Type type, Blacklist blacklist, RelType relType)
	{
		super(3, blacklist);

		if (type == null)
		{
			throw new IllegalArgumentException("The \"type\" parameter cannot be null.");
		}

		this.sideType = type;
		this.relType = relType;
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

		Set<PhysicalEntity> parts;

		if (conv.getLeft().contains(pe1))
		{
			parts = sideType == Type.OTHER_SIDE ? conv.getRight() : conv.getLeft();
		}
		else if (conv.getRight().contains(pe1))
		{
			parts = sideType == Type.SAME_SIDE ? conv.getRight() : conv.getLeft();
		}
		else throw new IllegalArgumentException(
				"The PhysicalEntity has to be a participant of the Conversion.");

		if (blacklist == null) return new HashSet<BioPAXElement>(parts);
		else
		{
			ConversionDirectionType dir = getDirection(conv);

			if ((dir == ConversionDirectionType.LEFT_TO_RIGHT && ((relType == RelType.INPUT && parts != conv.getLeft()) || (relType == RelType.OUTPUT && parts != conv.getRight()))) ||
				(dir == ConversionDirectionType.RIGHT_TO_LEFT && ((relType == RelType.INPUT && parts != conv.getRight()) || (relType == RelType.OUTPUT && parts != conv.getLeft()))))
				return Collections.emptySet();

			return new HashSet<BioPAXElement>(blacklist.getNonUbiques(parts, relType));
		}
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
