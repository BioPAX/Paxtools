package org.biopax.paxtools.causality.pattern.c;

import org.biopax.paxtools.causality.pattern.Match;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.ConversionDirectionType;
import org.biopax.paxtools.model.level3.Interaction;
import org.biopax.paxtools.model.level3.PhysicalEntity;

import java.util.Collection;
import java.util.HashSet;

/**
 * var0 is a PE
 * var1 is a Conversion
 *
 * @author Ozgun Babur
 */
public class ParticipatedInConv extends ConstraintAdapter
{
	Type type;

	public ParticipatedInConv(Type type)
	{
		this.type = type;
	}

	@Override
	public boolean canGenerate()
	{
		return true;
	}

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

				if (cnv.getConversionDirection() == ConversionDirectionType.REVERSIBLE)
				{
					result.add(cnv);
				}
				else if (cnv.getConversionDirection() == ConversionDirectionType.RIGHT_TO_LEFT &&
					type == Type.INPUT ? cnv.getRight().contains(pe) : cnv.getLeft().contains(pe))
				{
					result.add(cnv);
				}
				// Note that null direction is treated as if LEFT_TO_RIGHT. This is not proper,
				// but it is the best approximation.
				else if ((cnv.getConversionDirection() == ConversionDirectionType.LEFT_TO_RIGHT ||
					cnv.getConversionDirection() == null) &&
					type == Type.INPUT ? cnv.getLeft().contains(pe) : cnv.getRight().contains(pe))
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
	
	public enum Type
	{
		INPUT,
		OUTPUT
	}
}
