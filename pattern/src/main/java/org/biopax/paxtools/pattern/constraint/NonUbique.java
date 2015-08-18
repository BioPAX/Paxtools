package org.biopax.paxtools.pattern.constraint;

import org.biopax.paxtools.model.level3.PhysicalEntity;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.pattern.util.Blacklist;

/**
 * This is a non-generative constraint that checks if the small molecule is ubiquitous in any
 * context.
 *
 * Var0 - PhysicalEntity
 *
 * @author Ozgun Babur
 */
public class NonUbique extends ConstraintAdapter
{
	public NonUbique(Blacklist blacklist)
	{
		super(1, blacklist);
	}

	@Override
	public boolean satisfies(Match match, int... ind)
	{
		PhysicalEntity pe = (PhysicalEntity) match.get(ind[0]);
		return !blacklist.isUbique(pe);
	}
}
