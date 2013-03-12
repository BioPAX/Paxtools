package org.biopax.paxtools.pattern;

import org.biopax.paxtools.model.BioPAXElement;

import java.util.Collection;

/**
 * This is a mapping from a constraint to the elements in a match.
 *
 * @author Ozgun Babur
 */
public class MappedConst implements Constraint
{
	/**
	 * The constraint to map.
	 */
	private Constraint constr;

	/**
	 * Indexes of elements in the match for the constraint to check validity.
	 */
	private int[] inds;

	/**
	 * Constructor with the constraint and the index mapping.
	 * @param constr constraint to map
	 * @param inds mapped indexes
	 */
	public MappedConst(Constraint constr, int ... inds)
	{
		this.constr = constr;
		this.inds = inds;
	}

	/**
	 * Getter for the constraint.
	 * @return the wrapped constraint
	 */
	public Constraint getConstr()
	{
		return constr;
	}

	/**
	 * Getter for the mapped indices.
	 * @return mapped indices
	 */
	public int[] getInds()
	{
		return inds;
	}

	/**
	 * This methods translates the indexes of outer constraint, to this inner constraint.
	 *
	 * @param outer mapped indices for the outer constraints
	 * @return translated indices
	 */
	protected int[] translate(int[] outer)
	{
		int[] t = new int[inds.length];
		for (int i = 0; i < t.length; i++)
		{
			t[i] = outer[inds[i]];
		}
		return t;
	}

	/**
	 * Can generate only if the wrapped constraint is generative.
	 * @return if the wrapped constraint is generative
	 */
	public boolean canGenerate()
	{
		return constr.canGenerate();
	}

	/**
	 * Calls generate method of the constraint with index translation.
	 * @param match current pattern match
	 * @param outer untranslated indices
	 * @return generated satisfying elements
	 */
	@Override
	public Collection<BioPAXElement> generate(Match match, int... outer)
	{
		return constr.generate(match, translate(outer));
	}

	/**
	 * Directs to satisfies method of the wrapped constraint with index translation.
	 * @param match current pattern match
	 * @param outer untranslated indices
	 * @return true if the constraint is satisfied
	 */
	public boolean satisfies(Match match, int ... outer)
	{
		return constr.satisfies(match, translate(outer));
	}

	/**
	 * Gets variable size of the wrapped constraint.
	 * @return variable size of the wrapped constraint
	 */
	@Override
	public int getVariableSize()
	{
		return constr.getVariableSize();
	}

}
