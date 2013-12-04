package org.biopax.paxtools.pattern.constraint;

import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.PhysicalEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Given Conversion and its one of the participants (at the left or right), traverses to the other
 * participants that are at the other side.
 *
 * var0 is a PE (PE1)
 * var1 is a Conv
 * var2 is a PE (PE2)
 *
 * Prerequisite: PE1 is either at left or right of Conv
 *
 * @author Ozgun Babur
 */
public class OtherSide extends ConstraintAdapter
{
	/**
	 * Constructor.
	 */
	public OtherSide()
	{
		super(3);
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
	 * Checks which side is the first PhysicalEntity, and gathers participants on the other side.
	 * @param match current pattern match
	 * @param ind mapped indices
	 * @return participants at the other side
	 */
	@Override
	public Collection<BioPAXElement> generate(Match match, int... ind)
	{
		assertIndLength(ind);

		PhysicalEntity pe1 = (PhysicalEntity) match.get(ind[0]);
		Conversion conv = (Conversion) match.get(ind[1]);

		if (conv.getLeft().contains(pe1))
		{
			return new HashSet<BioPAXElement>(conv.getRight());
		}
		else if (conv.getRight().contains(pe1))
		{
			return new HashSet<BioPAXElement>(conv.getLeft());
		}
		return Collections.emptySet();
	}
}
