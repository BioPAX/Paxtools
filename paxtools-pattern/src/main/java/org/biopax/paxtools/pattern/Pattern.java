package org.biopax.paxtools.pattern;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ozgun Babur
 */
public class Pattern
{
	protected int variableSize;
	protected List<MappedConst> constraints;

	public Pattern(int variableSize, List<MappedConst> constraints)
	{
		this.variableSize = variableSize;
		this.constraints = constraints;
	}

	public Pattern(int variableSize)
	{
		this(variableSize, new ArrayList<MappedConst>());
	}

	public void addConstraint(Constraint constr, int ... ind)
	{
		assert ind.length > 0;
		assert checkIndsInRange(ind);
		constraints.add(new MappedConst(constr, ind));
	}

	/**
	 * Appends the constraints in the parameter pattern to the desired location. Indexes in the
	 * constraint mappings are translated so that 0 is translated to ind0, and others are translated
	 * to orig + indAppend - 1. It is user's responsibility to make sure that the variable size has
	 * room for the new variables added. Number of new variables is equal to the variable size of 
	 * the parameter pattern - 1.
	 * 
	 * @param p
	 * @param ind0
	 * @param indAppend
	 */
	public void addPattern(Pattern p, int ind0, int indAppend)
	{
		assert ind0 < indAppend;

		for (MappedConst mc : p.constraints)
		{
			Constraint c = mc.getConstr();
			int[] inds = mc.getInds();

			int[] t = new int[inds.length];
			for (int j = 0; j < t.length; j++)
			{
				t[j] = inds[j] == 0 ? ind0 : (inds[j] + indAppend - 1);
			}

			addConstraint(c, t);
		}
	}
	

	public List<MappedConst> getConstraints()
	{
		return constraints;
	}

	private boolean checkIndsInRange(int ... ind)
	{
		for (int i : ind) if (i >= variableSize) return false;
		return true;
	}
	
	public int getVariableSize()
	{
		return variableSize;
	}
}
