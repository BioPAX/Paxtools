package org.biopax.paxtools.pattern.c;

import org.biopax.paxtools.pattern.Match;
import org.biopax.paxtools.model.BioPAXElement;

/**
 * Checks if a variable is a specific type or its superclass.
 *
 * @author Ozgun Babur
 */
public class Type extends ConstraintAdapter
{
	private Class<? extends BioPAXElement> clazz;

	public Type(Class<? extends BioPAXElement> clazz)
	{
		this.clazz = clazz;
	}

	@Override
	public boolean satisfies(Match match, int... ind)
	{
		assert ind.length == 1;

		return clazz.isAssignableFrom(match.get(ind[0]).getModelInterface());
	}

	@Override
	public int getVariableSize()
	{
		return 1;
	}
}
