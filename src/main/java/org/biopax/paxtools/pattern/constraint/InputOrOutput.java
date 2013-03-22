package org.biopax.paxtools.pattern.constraint;

import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.ConversionDirectionType;
import org.biopax.paxtools.pattern.Match;

import java.util.Collection;
import java.util.HashSet;

/**
 * Gets input or output participants of a Conversion.
 *
 * var0 is a Conversion
 * var1 is a PE
 *
 * @author Ozgun Babur
 */
public class InputOrOutput extends ConstraintAdapter
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
	public InputOrOutput(RelType type, boolean treatReversibleAsLeftToRight)
	{
		super(2);
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

		Conversion cnv = (Conversion) match.get(ind[0]);

		if (cnv.getConversionDirection() == ConversionDirectionType.REVERSIBLE &&
			!treatReversibleAsLeftToRight)
		{
			result.addAll(cnv.getLeft());
			result.addAll(cnv.getRight());
		}
		else if (cnv.getConversionDirection() == ConversionDirectionType.RIGHT_TO_LEFT)
		{
			result.addAll(type == RelType.INPUT ? cnv.getRight() : cnv.getLeft());
		}
		// Note that null direction is treated as if LEFT_TO_RIGHT. This is not a best
		// practice, but it is a good approximation.
		else if ((cnv.getConversionDirection() == ConversionDirectionType.LEFT_TO_RIGHT ||
			cnv.getConversionDirection() == null ||
			(cnv.getConversionDirection() == ConversionDirectionType.REVERSIBLE &&
				treatReversibleAsLeftToRight)))
		{
			result.addAll(type == RelType.OUTPUT ? cnv.getRight() : cnv.getLeft());
		}

		return result;
	}
}
