package org.biopax.paxtools.pattern.c;

import org.biopax.paxtools.pattern.Match;

import java.util.Set;

/**
 * This is a separate class (not reusing FieldConstraint) because PathAccessors cannot access to
 * RDFIDs.
 *
 * @author Ozgun Babur
 */
public class IDConstraint extends ConstraintAdapter
{
	Set<String> ids;

	public IDConstraint(Set<String> ids)
	{
		this.ids = ids;
	}

	@Override
	public int getVariableSize()
	{
		return 1;
	}

	@Override
	public boolean satisfies(Match match, int... ind)
	{
		return ids.contains(match.get(ind[0]).getRDFId());
	}
}
