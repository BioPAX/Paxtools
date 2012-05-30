package org.biopax.paxtools.pattern;

/**
 * This is a mapping from a constraint to the elements in a match.
 *
 * @author Ozgun Babur
 */
public class MappedConst
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
}
