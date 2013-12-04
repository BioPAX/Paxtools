package org.biopax.paxtools.pattern.constraint;

import org.biopax.paxtools.pattern.Constraint;
import org.biopax.paxtools.pattern.Match;

/**
 * Negation of a constraint. This is not generative.
 *
 * @author Ozgun Babur
 */
public class NOT extends ConstraintAdapter
{
	/**
	 * Constraint to negate
	 */
	Constraint con;

	/**
	 * Constructor with the wrapped constraint.
	 * @param con constraint to negate
	 */
	public NOT(Constraint con)
	{
		this.con = con;
	}

	/**
	 * Size is equal to the of the negated constraint
	 * @return size of the wrapped constraint
	 */
	@Override
	public int getVariableSize()
	{
		return con.getVariableSize();
	}

	/**
	 * Negates the satisfies value of the wrapped constraint.
	 * @param match current pattern match
	 * @param ind mapped indices
	 * @return negated value
	 */
	@Override
	public boolean satisfies(Match match, int... ind)
	{
		return !con.satisfies(match, ind);
	}
}
