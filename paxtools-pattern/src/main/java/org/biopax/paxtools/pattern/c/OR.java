package org.biopax.paxtools.pattern.c;

import org.biopax.paxtools.pattern.Constraint;
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
	Constraint[] con;

	public OR(Constraint... con)
	{
		this.con = con;
	}

	@Override
	public boolean satisfies(Match match, int... ind)
	{
		for (Constraint constr : con)
		{
			if (constr.satisfies(match, ind)) return true;
		}
		return false;
	}

	@Override
	public boolean canGenerate()
	{
		return con[0].canGenerate();
	}

	@Override
	public int getVariableSize()
	{
		return con[0].getVariableSize();
	}

	@Override
	public Collection<BioPAXElement> generate(Match match, int... ind)
	{
		Collection<BioPAXElement> gen = new HashSet<BioPAXElement>();

		for (Constraint aCon : con)
		{
			gen.addAll(aCon.generate(match, ind));
		}
		return gen;
	}

}
