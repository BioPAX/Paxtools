package org.biopax.paxtools.causality.pattern;

import org.biopax.paxtools.controller.PathAccessor;
import org.biopax.paxtools.model.BioPAXElement;

import java.util.Collection;

/**
 * Logical OR of several PathConstraints.
 *
 * @author Ozgun Babur
 */
public class MultiPathConstraint extends ConstraintAdapter
{
	PathConstraint[] pc;

	public MultiPathConstraint(String ... paths)
	{
		pc = new PathConstraint[paths.length];

		for (int i = 0; i < pc.length; i++)
		{
			pc[i] = new PathConstraint(paths[i]);
		}
	}

	@Override
	public boolean satisfies(Match match, int... ind)
	{
		for (PathConstraint patC : pc)
		{
			if (patC.satisfies(match, ind)) return true;
		}
		return false;
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
		Collection<BioPAXElement> gen = pc[0].generate(match, ind);

		for (int i = 1; i < pc.length; i++)
		{
			gen.addAll(pc[i].generate(match, ind));
		}
		return gen;
	}
}
