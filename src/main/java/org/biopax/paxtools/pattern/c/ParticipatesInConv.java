package org.biopax.paxtools.pattern.c;

import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.ConversionDirectionType;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.PhysicalEntity;

import java.util.Collection;
import java.util.HashSet;

/**
 * Gets input or output participants of a Conversion.
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
	RelType type;

	/**
	 * Sometimes users may opt to treat reversible conversions as if left to right just to avoid to
	 * traverse towards both sides.
	 */
	boolean treatReversibleAsLeftToRight;

	/**
	 * Constructor with parameters.
	 * @param type input or output
	 * @param treatReversibleAsLeftToRight option to not to traverse both sides of a reversible
	 * conversion
	 */
	public ParticipatesInConv(RelType type, boolean treatReversibleAsLeftToRight)
	{
		this.type = type;
		this.treatReversibleAsLeftToRight = treatReversibleAsLeftToRight;
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
		Collection<BioPAXElement> result = new HashSet<BioPAXElement>();

		PhysicalEntity pe = (PhysicalEntity) match.get(ind[0]);

		for (Interaction inter : pe.getParticipantOf())
		{
			if (inter instanceof Conversion)
			{
				Conversion cnv = (Conversion) inter;

				if (cnv.getConversionDirection() == ConversionDirectionType.REVERSIBLE &&
					!treatReversibleAsLeftToRight)
				{
					result.add(cnv);
				}
				else if (cnv.getConversionDirection() == ConversionDirectionType.RIGHT_TO_LEFT &&
					(type == RelType.INPUT ? cnv.getRight().contains(pe) : cnv.getLeft().contains(pe)))
				{
					result.add(cnv);
				}
				// Note that null direction is treated as if LEFT_TO_RIGHT. This is not a best
				// practice, but it is a good approximation.
				else if ((cnv.getConversionDirection() == ConversionDirectionType.LEFT_TO_RIGHT ||
					cnv.getConversionDirection() == null ||
					(cnv.getConversionDirection() == ConversionDirectionType.REVERSIBLE &&
						treatReversibleAsLeftToRight)) &&
					(type == RelType.INPUT ? cnv.getLeft().contains(pe) : cnv.getRight().contains(pe)))
				{
					result.add(cnv);
				}
			}
		}

		return result;
	}

	@Override
	public int getVariableSize()
	{
		return 2;
	}
}
