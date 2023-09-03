package org.biopax.paxtools.pattern.constraint;

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
	/**
	 * Wrapped constraints.
	 */
	MappedConst[] con;

	/**
	 * Constructor with the array of mapped constraints.
	 * @param con wrapped constraints
	 */
	public OR(MappedConst... con)
	{
		this.con = con;
	}

	/**
	 * Checks if any of the wrapped constraints satisfy.
	 * @param match current pattern match
	 * @param ind mapped indices
	 * @return true if any of the wrapped constraints satisfy
	 */
	@Override
	public boolean satisfies(Match match, int... ind)
	{
		for (MappedConst mc : con)
		{
			if (mc.satisfies(match, ind)) return true;
		}
		return false;
	}

	/**
	 * Can generate only if all of the wrapped constraints are generative.
	 * @return true if none of the wrapped constraints are non-generative
	 */
	@Override
	public boolean canGenerate()
	{
		for (MappedConst mc : con)
		{
			if (!mc.canGenerate()) return false;
		}
		return true;
	}

	/**
	 * Checks the inner mapping of the wrapped constraints and figures the size.
	 * @return the size
	 */
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

	/**
	 * Gets the max value.
	 * @param v array to check
	 * @return max value
	 */
	protected int max(int[] v)
	{
		int x = 0;
		for (int i : v)
		{
			if (i > x) x = i;
		}
		return x;
	}

	/**
	 * Gets the intersection of the generated values of wrapped constraints.
	 * @param match current pattern match
	 * @param ind mapped indices
	 * @return intersection of the generated values of wrapped constraints
	 */
	@Override
	public Collection<BioPAXElement> generate(Match match, int... ind)
	{
		Collection<BioPAXElement> gen = new HashSet<>();

		for (MappedConst mc : con)
		{
			gen.addAll(mc.generate(match, ind));
		}
		return gen;
	}
}
