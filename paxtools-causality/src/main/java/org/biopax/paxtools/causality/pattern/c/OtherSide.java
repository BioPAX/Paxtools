package org.biopax.paxtools.causality.pattern.c;

import org.biopax.paxtools.causality.pattern.Match;
import org.biopax.paxtools.model.BioPAXElement;
import org.biopax.paxtools.model.level3.Conversion;
import org.biopax.paxtools.model.level3.PhysicalEntity;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
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
	@Override
	public int getVariableSize()
	{
		return 3;
	}

	@Override
	public boolean canGenerate()
	{
		return true;
	}

	@Override
	public Collection<BioPAXElement> generate(Match match, int... ind)
	{
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
