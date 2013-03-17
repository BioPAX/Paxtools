package org.biopax.paxtools.pattern.constraint;

import org.biopax.paxtools.pattern.Constraint;
import org.biopax.paxtools.pattern.Match;

/**
 * Checks if the parameter constraint cannot generate any candidate.
 *
 * @author Ozgun Babur
 */
public class Empty extends ConstraintAdapter
{
	/**
	 * The generative constraint to check if it generates nothing.
	 */
	Constraint con;

	/**
	 * Constructor with the generative Constraint.
	 * @param con the generative Constraint
	 */
	public Empty(Constraint con)
	{
		if (!con.canGenerate()) throw new IllegalArgumentException(
			"The constraint has to be a generative constraint");

		this.con = con;
	}

	/**
	 * Variable size is one less than the wrapped Constraint.
	 * @return one less than the size of teh wrapped constraint
	 */
	@Override
	public int getVariableSize()
	{
		return con.getVariableSize() - 1;
	}

	/**
	 * Cannot generate
	 * @return false
	 */
	@Override
	public boolean canGenerate()
	{
		return false;
	}

	/**
	 * CHecks if the wrapped Constraint can generate any elements. THis satisfies if it cannot.
	 * @param match current pattern match
	 * @param ind mapped indices
	 * @return true if the wrapped Constraint generates nothing
	 */
	@Override
	public boolean satisfies(Match match, int... ind)
	{
		assertIndLength(ind);

		return con.generate(match, ind).isEmpty();
	}
}
