package org.biopax.paxtools.pattern;

/**
 * @author Ozgun Babur
 */
public class MappedConst
{
	private Constraint constr;
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
