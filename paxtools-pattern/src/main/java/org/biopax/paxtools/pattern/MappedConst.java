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

	public MappedConst(Constraint constr, int ... inds)
	{
		this.constr = constr;
		this.inds = inds;
	}

	public Constraint getConstr()
	{
		return constr;
	}

	public int[] getInds()
	{
		return inds;
	}

	/**
	 * This methods translates the indexes of outer constraint, to this inner constraint.
	 *
	 * @param outer
	 * @return
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

	public boolean canGenerate()
	{
		return constr.canGenerate();
	}

	@Override
	public Collection<BioPAXElement> generate(Match match, int... outer)
	{
		return constr.generate(match, translate(outer));
	}

	public boolean satisfies(Match match, int ... outer)
	{
		return constr.satisfies(match, translate(outer));
	}

	@Override
	public int getVariableSize()
	{
		return constr.getVariableSize();
	}

}
