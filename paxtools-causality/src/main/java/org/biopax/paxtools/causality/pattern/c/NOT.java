package org.biopax.paxtools.causality.pattern.c;

import org.biopax.paxtools.causality.pattern.Constraint;
import org.biopax.paxtools.causality.pattern.Match;

/**
 * Negation of a constraint. This is not generative.
 *
 * @author Ozgun Babur
 */
public class NOT extends ConstraintAdapter
{
	Constraint con;

	public NOT(Constraint con)
	{
		this.con = con;
	}

	@Override
	public int getVariableSize()
	{
		return con.getVariableSize();
	}

	@Override
	public boolean satisfies(Match match, int... ind)
	{
		return !super.satisfies(match, ind);
	}
}
