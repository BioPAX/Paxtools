package org.biopax.paxtools.pattern.constraint;

import org.biopax.paxtools.pattern.Constraint;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.model.BioPAXElement;

import java.util.Collection;

/**
 * When a constraint excludes the origin element, but it is needed to be among them, use this 
 * constraint. The wrapped constraint must have size 2.
 * 
 * @author Ozgun Babur
 */
public class SelfOrThis extends ConstraintAdapter
{
	/**
	 * Wrapped constraint.
	 */
	Constraint con;

	/**
	 * Constructor with the wrapped constraint.
	 * @param con wrapped constraint
	 */
	public SelfOrThis(Constraint con)
	{
		this.con = con;
		if (con.getVariableSize() != 2)
			throw new IllegalArgumentException("Parameter constraint must be size 2.");
	}

	/**
	 * Always 2.
	 * @return 2
	 */
	@Override
	public int getVariableSize()
	{
		return 2;
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
	 * Gets the first mapped element along with the generated elements of wrapped constraint.
	 * @param match current pattern match
	 * @param ind mapped indices
	 * @return first mapped element along with the generated elements of wrapped constraint
	 */
	@Override
	public Collection<BioPAXElement> generate(Match match, int... ind)
	{
		Collection<BioPAXElement> gen = con.generate(match, ind);
		gen.add(match.get(ind[0]));
		return gen;
	}

	/**
	 * Checks if the last index is either generated or equal to the first element.
	 * @param match current pattern match
	 * @param ind mapped indices
	 * @return true if the last index is either generated or equal to the first element
	 */
	@Override
	public boolean satisfies(Match match, int... ind)
	{
		return match.get(ind[0]) == match.get(ind[1]) || super.satisfies(match, ind);
	}
}
