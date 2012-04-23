package org.biopax.paxtools.causality.pattern;

import org.biopax.paxtools.model.BioPAXElement;

import java.util.Collection;

/**
 * When a constraint excludes the origin element, but it is needed to be among them, use this 
 * constraint.
 * 
 * @author Ozgun Babur
 */
public class SelfOrThisConstraint extends ConstraintAdapter
{
	Constraint con;

	public SelfOrThisConstraint(Constraint con)
	{
		this.con = con;
		assert con.getVariableSize() == 2;
	}

	@Override
	public int getVariableSize()
	{
		return 2;
	}

	@Override
	public boolean canGenerate()
	{
		return true;
	}

	@Override
	public Collection<BioPAXElement> generate(Match match, int... ind)
	{
		Collection<BioPAXElement> gen = super.generate(match, ind);
		gen.add(match.get(ind[0]));
		return gen;
	}

	@Override
	public boolean satisfies(Match match, int... ind)
	{
		return match.get(ind[0]) == match.get(ind[1]) || super.satisfies(match, ind);
	}
}
