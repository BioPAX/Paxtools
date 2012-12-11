package org.biopax.paxtools.pattern.c;

import org.biopax.paxtools.pattern.Constraint;
import org.biopax.paxtools.pattern.MappedConst;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.model.BioPAXElement;

import java.util.Collection;
import java.util.HashSet;

/**
 * Logical OR of several constraints. Each constraint should have the same variable size and should
 * be able to process same variables at the same order. Constraints should be all generative or all
 * non-generative, and they cannot mix.
 *
 * @author Ozgun Babur
 */
public class OR extends ConstraintAdapter
{
	MappedConst[] con;

	public OR(MappedConst... con)
	{
		this.con = con;
	}

	@Override
	public boolean satisfies(Match match, int... ind)
	{
		for (MappedConst mc : con)
		{
			if (mc.satisfies(match, ind)) return true;
		}
		return false;
	}
	

	@Override
	public boolean canGenerate()
	{
		for (MappedConst mc : con)
		{
			if (!mc.canGenerate()) return false;
		}
		return true;
	}

	@Override
	public int getVariableSize()
	{
		int size = 0;
		for (MappedConst mc : con)
		{
			int m = max(mc.getInds());
			if (m > size) size = m;
		}
		return size + 1;
	}

	protected int max(int[] v)
	{
		int x = 0;
		for (int i : v)
		{
			if (i > x) x = i;
		}
		return x;
	}
	
	@Override
	public Collection<BioPAXElement> generate(Match match, int... ind)
	{
		Collection<BioPAXElement> gen = new HashSet<BioPAXElement>();

		for (MappedConst mc : con)
		{
			gen.addAll(mc.generate(match, ind));
		}
		return gen;
	}

}
