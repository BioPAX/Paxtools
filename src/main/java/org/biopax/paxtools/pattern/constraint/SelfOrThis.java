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
	int selfIndex;

	/**
	 * Wrapped constraint.
	 */
	Constraint con;

	/**
	 * Constructor with the wrapped constraint. Index of self is 0 by default.
	 * @param con wrapped constraint
	 */
	public SelfOrThis(Constraint con)
	{
		this(con, 0);
	}

	/**
	 * Constructor with the wrapped constraint and index of self. This index cannot map to the last
	 * element, but has to map one of the previous ones.
	 * @param con wrapped constraint
	 * @param selfIndex index of self
	 */
	public SelfOrThis(Constraint con, int selfIndex)
	{
		this.selfIndex = selfIndex;
		this.con = con;
		if (!con.canGenerate()) throw new IllegalArgumentException(
			"The wrapped constraint has to be generative");
		if (selfIndex >= con.getVariableSize() - 1) throw new IllegalArgumentException(
			"selfIndex has to be smaller than the index of last mapped element. selfIndex = " +
				selfIndex + ", size = " + con.getVariableSize());
	}

	/**
	 * Returns size of the wrapped constraint.
	 * @return size of the wrapped constraint
	 */
	@Override
	public int getVariableSize()
	{
		return con.getVariableSize();
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
		gen.add(match.get(ind[selfIndex]));
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
		return match.get(ind[selfIndex]) == match.get(ind[ind.length-1]) ||
			super.satisfies(match, ind);
	}
}
