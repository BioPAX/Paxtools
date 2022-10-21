package org.biopax.paxtools.pattern.constraint;

import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.ConversionDirectionType;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.pattern.util.Blacklist;
import org.biopax.paxtools.pattern.util.RelType;

import java.util.Collection;
import java.util.HashSet;

/**
 * Gets the related Conversion where the PhysicalEntity is input or output, whichever is desired.
 *
 * var0 is a PE
 * var1 is a Conversion
 *
 * @author Ozgun Babur
 */
public class ParticipatesInConv extends ConstraintAdapter
{
	/**
	 * Input or output.
	 */
	private RelType type;

	/**
	 * Constructor with parameters.
	 * @param type input or output conversion
	 * @param blacklist for detecting blacklisted small molecules
	 */
	public ParticipatesInConv(RelType type, Blacklist blacklist)
	{
		super(2, blacklist);
		this.type = type;
	}

	/**
	 * Constructor with parameters.
	 * @param type input or output
	 * conversion
	 */
	public ParticipatesInConv(RelType type)
	{
		this(type, null);
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
	 * Identifies the conversion direction and gets the related participants.
	 * @param match current pattern match
	 * @param ind mapped indices
	 * @return input or output participants
	 */
	@Override
	public Collection<BioPAXElement> generate(Match match, int... ind)
	{
		Collection<BioPAXElement> result = new HashSet<>();

		PhysicalEntity pe = (PhysicalEntity) match.get(ind[0]);

		for (Interaction inter : pe.getParticipantOf())
		{
			if (inter instanceof Conversion)
			{
				Conversion cnv = (Conversion) inter;
				ConversionDirectionType dir = getDirection(cnv);

				// do not get blacklisted small molecules
				if (blacklist != null && blacklist.isUbique(pe, cnv, dir, type)) continue;

				if (dir == ConversionDirectionType.REVERSIBLE)
				{
					result.add(cnv);
				}
				else if (dir == ConversionDirectionType.RIGHT_TO_LEFT &&
					(type == RelType.INPUT ? cnv.getRight().contains(pe) : cnv.getLeft().contains(pe)))
				{
					result.add(cnv);
				}
				// Note that null direction is treated as if LEFT_TO_RIGHT. This is not a best
				// practice, but it is a good approximation.
				else if ((dir == ConversionDirectionType.LEFT_TO_RIGHT || dir == null) &&
					(type == RelType.INPUT ? cnv.getLeft().contains(pe) : cnv.getRight().contains(pe)))
				{
					result.add(cnv);
				}
			}
		}

		return result;
	}
}
