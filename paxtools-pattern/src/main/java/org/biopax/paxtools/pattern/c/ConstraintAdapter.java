package org.biopax.paxtools.pattern.c;

import org.biopax.paxtools.pattern.Constraint;
import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.model.BioPAXElement;

import java.util.Collection;

/**
 * @author Ozgun Babur
 */
public abstract class ConstraintAdapter implements Constraint
{
	/**
	 * If you override this method, then don't forget to also override getGeneratedInd, and generate
	 * methods.
	 */
	@Override
	public boolean canGenerate()
	{
		return false;
	}

	@Override
	public Collection<BioPAXElement> generate(Match match, int ... ind)
	{
		throw new RuntimeException("This constraint is not generative. " +
			"Please check with canGenerate first.");
	}

	/**
	 * Use this method only if constraint canGenerate, and satisfaction criteria is that simple.
	 *
	 * @param match
	 * @param ind
	 * @return
	 */
	@Override
	public boolean satisfies(Match match, int... ind)
	{
		return generate(match, ind).contains(match.get(ind[ind.length - 1]));
	}
}
