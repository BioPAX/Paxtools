package org.biopax.paxtools.pattern;

import org.biopax.paxtools.model.BioPAXElement;

import java.util.ArrayList;
import java.util.List;

/**
 * A pattern is a list of mapped constraints.
 *
 * @author Ozgun Babur
 */
public class Pattern
{
	/**
	 * How many elements are there in a pattern match.
	 */
	protected int variableSize;

	/**
	 * Class of the first elements to match with this pattern.
	 */
	protected Class<? extends BioPAXElement> startingClass;

	/**
	 * List of mapped constraints.
	 */
	protected List<MappedConst> constraints;

	public Pattern(int variableSize, Class<? extends BioPAXElement> startingClass,
		List<MappedConst> constraints)
	{
		this.variableSize = variableSize;
		this.startingClass = startingClass;
		this.constraints = constraints;
	}

	public Pattern(int variableSize, Class<? extends BioPAXElement> startingClass)
	{
		this(variableSize, startingClass, new ArrayList<MappedConst>());
	}

	/**
	 * Creates a mapped constraint with the given constraint and the indexes it applies.
	 * @param constr
	 * @param ind
	 */
	public void addConstraint(Constraint constr, int ... ind)
	{
		assert ind.length > 0;
		assert checkIndsInRange(ind);
		assert constr.getVariableSize() == ind.length;
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

	/**
	 * A point constraint deals with only one element in a match, checks its validity. This method
	 * injects the parameter constraint multiple times among the list of mapped constraints, to the
	 * specified indexes.
	 *
	 * @param con
	 * @param ind
	 */
	public void insertPointConstraint(Constraint con, int ... ind)
	{
		assert con.getVariableSize() == 1;

		for (int i : ind)
		{
			for (int j = 0; j < constraints.size(); j++)
			{
				int[] index = constraints.get(j).getInds();
				if (index[index.length-1] == i)
				{
					constraints.add(j + 1, new MappedConst(con, i));
					break;
				}
			}
		}
	}

	public List<MappedConst> getConstraints()
	{
		return constraints;
	}

	/**
	 * Checks if these indices are in the range of variable size.
	 * @param ind
	 * @return
	 */
	private boolean checkIndsInRange(int ... ind)
	{
		for (int i : ind) if (i >= variableSize) return false;
		return true;
	}
	
	public int getVariableSize()
	{
		return variableSize;
	}

	public void setVariableSize(int variableSize)
	{
		this.variableSize = variableSize;
	}
	
	public void increaseVariableSizeBy(int inc)
	{
		this.variableSize += inc;
	}

	/**
	 * @return The class of first element in a match
	 */
	public Class<? extends BioPAXElement> getStartingClass()
	{
		return startingClass;
	}
}
