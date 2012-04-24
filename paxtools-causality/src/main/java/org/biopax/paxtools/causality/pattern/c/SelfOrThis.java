package org.biopax.paxtools.causality.pattern.c;

import org.biopax.paxtools.causality.pattern.Constraint;
import org.biopax.paxtools.causality.pattern.Match;
import org.biopax.paxtools.model.BioPAXElement;

import java.util.Collection;

/**
 * When a constraint excludes the origin element, but it is needed to be among them, use this 
 * constraint.
 * 
 * @author Ozgun Babur
 */
public class SelfOrThis extends ConstraintAdapter
{
	Constraint con;

	public SelfOrThis(Constraint con)
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
		Collection<BioPAXElement> gen = con.generate(match, ind);
		gen.add(match.get(ind[0]));
		return gen;
	}

	@Override
	public boolean satisfies(Match match, int... ind)
	{
		return match.get(ind[0]) == match.get(ind[1]) || super.satisfies(match, ind);
	}
}
