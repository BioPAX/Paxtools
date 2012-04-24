package org.biopax.paxtools.causality.pattern;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ozgun Babur
 */
public class Pattern
{
	int variableSize;
	List<MappedConst> constraints;

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
		assert checkIndsInRange(ind);
		constraints.add(new MappedConst(constr, ind));
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
