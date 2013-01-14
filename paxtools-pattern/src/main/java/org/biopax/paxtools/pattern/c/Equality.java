package org.biopax.paxtools.pattern.c;

import org.biopax.paxtools.pattern.Match;

/**
 * @author Ozgun Babur
 */
public class Equality extends ConstraintAdapter
{
	private boolean equals;

	public Equality(boolean equals)
	{
		this.equals = equals;
	}

	@Override
	public boolean satisfies(Match match, int... ind)
	{
		assert ind.length == 2;

		return (match.get(ind[0]) == match.get(ind[1])) == equals;
	}

	@Override
	public int getVariableSize()
	{
		return 2;
	}
}
