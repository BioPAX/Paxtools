package org.biopax.paxtools.pattern.c;

import org.biopax.paxtools.pattern.Constraint;
import org.biopax.paxtools.pattern.Match;

/**
 * Checks if the parameter constraint cannot generate any candidate.
 *
 * @author Ozgun Babur
 */
public class Empty extends ConstraintAdapter
{
	Constraint con;

	public Empty(Constraint con)
	{
		if (!con.canGenerate()) throw new IllegalArgumentException(
			"The constraint has to be a generative constraint");

		this.con = con;
	}

	@Override
	public int getVariableSize()
	{
		return con.getVariableSize() - 1;
	}

	@Override
	public boolean canGenerate()
	{
		return false;
	}

	@Override
	public boolean satisfies(Match match, int... ind)
	{
		assertIndLength(ind);

		return con.generate(match, ind).isEmpty();
	}
}
